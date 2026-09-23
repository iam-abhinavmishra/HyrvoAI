import { useEffect, useState } from 'react';

import { useAuth } from '../context/AuthContext';

import ThemeToggle from '../components/ThemeToggle';

import {
    getAdminDocuments,
    uploadAdminDocument,
    deactivateAdminDocument,
} from '../services/api';


function AdminDocuments() {

    const { user, token } = useAuth();


    const [documents, setDocuments] = useState([]);

    const [loading, setLoading] = useState(true);

    const [uploading, setUploading] = useState(false);

    const [deactivatingId, setDeactivatingId] = useState(null);


    const [error, setError] = useState('');

    const [success, setSuccess] = useState('');


    const [file, setFile] = useState(null);

    const [title, setTitle] = useState('');

    const [department, setDepartment] = useState('GENERAL');

    const [version, setVersion] = useState('1.0');

    const [accessLevel, setAccessLevel] = useState('EMPLOYEE');


    async function loadDocuments() {

        try {

            setLoading(true);

            setError('');


            const data = await getAdminDocuments(token);


            setDocuments(data);

        } catch (err) {

            setError(
                err.message ||
                'Failed to load documents.'
            );

        } finally {

            setLoading(false);

        }

    }


    useEffect(() => {

        if (token && user?.role === 'ADMIN') {

            loadDocuments();

        }

    }, [token, user]);


    async function handleUpload(event) {

        event.preventDefault();


        setError('');

        setSuccess('');


        if (!file) {

            setError(
                'Please select a PDF or DOCX file.'
            );

            return;

        }


        /*
         * Allowed file types
         */

        const allowedTypes = [

            'application/pdf',

            'application/msword',

            'application/vnd.openxmlformats-officedocument.wordprocessingml.document',

        ];


        if (!allowedTypes.includes(file.type)) {

            setError(
                'Only PDF and DOC/DOCX files are supported.'
            );

            return;

        }


        /*
         * Maximum file size: 10 MB
         */

        const maxSize = 10 * 1024 * 1024;


        if (file.size > maxSize) {

            setError(
                'File size must be less than 10 MB.'
            );

            return;

        }


        try {

            setUploading(true);


            await uploadAdminDocument(
                token,
                file,
                title,
                department,
                version,
                accessLevel
            );


            setSuccess(
                'Document uploaded successfully.'
            );


            /*
             * Reset form
             */

            setFile(null);

            setTitle('');

            setDepartment('GENERAL');

            setVersion('1.0');

            setAccessLevel('EMPLOYEE');


            const fileInput =
                document.getElementById(
                    'document-file'
                );


            if (fileInput) {

                fileInput.value = '';

            }


            /*
             * Refresh document list
             */

            await loadDocuments();


        } catch (err) {

            setError(
                err.message ||
                'Failed to upload document.'
            );

        } finally {

            setUploading(false);

        }

    }


    async function handleDeactivate(documentId) {

        const confirmed = window.confirm(
            'Are you sure you want to deactivate this document?'
        );


        if (!confirmed) {

            return;

        }


        try {

            setDeactivatingId(documentId);

            setError('');

            setSuccess('');


            await deactivateAdminDocument(
                token,
                documentId
            );


            setSuccess(
                'Document deactivated successfully.'
            );


            /*
             * Refresh active documents
             */

            await loadDocuments();


        } catch (err) {

            setError(
                err.message ||
                'Failed to deactivate document.'
            );

        } finally {

            setDeactivatingId(null);

        }

    }


    /*
     * Frontend admin protection
     */

    if (user?.role !== 'ADMIN') {

        return (

            <div className="admin-access-denied">

                <h2>
                    Access denied
                </h2>

                <p>
                    You do not have permission to access
                    this page.
                </p>

            </div>

        );

    }


    return (

        <div className="admin-page">


            {/* ================= HEADER ================= */}

            <header className="admin-header">

                <div>

                    <h1>
                        Document Management
                    </h1>

                    <p>
                        Manage the documents used by HyrvoAI.
                    </p>

                </div>


                <ThemeToggle />


                <a
                    href="/chat"
                    className="admin-chat-link"
                >
                    ← Back to Chat
                </a>

            </header>


            {/* ================= ALERTS ================= */}

            {error && (

                <div className="admin-alert error">

                    {error}

                </div>

            )}


            {success && (

                <div className="admin-alert success">

                    {success}

                </div>

            )}


            {/* ================= UPLOAD ================= */}

            <section className="admin-card upload-card">

                <div className="admin-card-header">

                    <div>

                        <h2>
                            Upload Document
                        </h2>

                        <p>
                            Add a new company policy, manual,
                            or internal document.
                        </p>

                    </div>

                </div>


                <form
                    className="document-upload-form"
                    onSubmit={handleUpload}
                >


                    {/* FILE */}

                    <div className="form-group">

                        <label htmlFor="document-file">
                            File
                        </label>

                        <input
                            id="document-file"
                            type="file"
                            accept=".pdf,.doc,.docx"
                            onChange={(event) =>
                                setFile(
                                    event.target.files?.[0] || null
                                )
                            }
                        />

                        <small>
                            Supported formats: PDF, DOC, DOCX
                            {' '}• Maximum size: 10 MB
                        </small>

                    </div>


                    {/* TITLE */}

                    <div className="form-group">

                        <label htmlFor="document-title">
                            Title
                        </label>

                        <input
                            id="document-title"
                            type="text"
                            placeholder="e.g. Leave Policy"
                            value={title}
                            onChange={(event) =>
                                setTitle(event.target.value)
                            }
                        />

                    </div>


                    {/* DEPARTMENT + VERSION */}

                    <div className="form-row">

                        <div className="form-group">

                            <label htmlFor="document-department">
                                Department
                            </label>

                            <select
                                id="document-department"
                                value={department}
                                onChange={(event) =>
                                    setDepartment(
                                        event.target.value
                                    )
                                }
                            >

                                <option value="GENERAL">
                                    General
                                </option>

                                <option value="HR">
                                    HR
                                </option>

                                <option value="IT">
                                    IT
                                </option>

                                <option value="FINANCE">
                                    Finance
                                </option>

                                <option value="SALES">
                                    Sales
                                </option>

                                <option value="ENGINEERING">
                                    Engineering
                                </option>

                            </select>

                        </div>


                        <div className="form-group">

                            <label htmlFor="document-version">
                                Version
                            </label>

                            <input
                                id="document-version"
                                type="text"
                                placeholder="1.0"
                                value={version}
                                onChange={(event) =>
                                    setVersion(
                                        event.target.value
                                    )
                                }
                            />

                        </div>

                    </div>


                    {/* ACCESS LEVEL */}

                    <div className="form-group">

                        <label htmlFor="document-access-level">
                            Access Level
                        </label>

                        <select
                            id="document-access-level"
                            value={accessLevel}
                            onChange={(event) =>
                                setAccessLevel(
                                    event.target.value
                                )
                            }
                        >

                            <option value="EMPLOYEE">
                                Employee Only
                            </option>

                            <option value="PUBLIC">
                                Public
                            </option>

                        </select>

                        <small>
                            Public documents can be used by the
                            public HyrvoAI widget. Employee-only
                            documents require authenticated access.
                        </small>

                    </div>


                    {/* UPLOAD BUTTON */}

                    <button
                        type="submit"
                        className="upload-button"
                        disabled={uploading}
                    >

                        {uploading
                            ? 'Uploading...'
                            : 'Upload Document'}

                    </button>

                </form>

            </section>


            {/* ================= ACTIVE DOCUMENTS ================= */}

            <section className="admin-card">

                <div className="admin-card-header">

                    <div>

                        <h2>
                            Active Documents
                        </h2>

                        <p>
                            Documents currently available to
                            HyrvoAI.
                        </p>

                    </div>

                    <span className="document-count">
                        {documents.length}
                    </span>

                </div>


                {/* LOADING */}

                {loading ? (

                    <div className="admin-loading">
                        Loading documents...
                    </div>

                ) : documents.length === 0 ? (

                    /* EMPTY */

                    <div className="admin-empty">

                        <h3>
                            No documents yet
                        </h3>

                        <p>
                            Upload your first company document
                            above.
                        </p>

                    </div>

                ) : (

                    /* TABLE */

                    <div className="documents-table-wrapper">

                        <table className="documents-table">

                            <thead>

                                <tr>

                                    <th>
                                        Document
                                    </th>

                                    <th>
                                        Department
                                    </th>

                                    <th>
                                        Version
                                    </th>

                                    <th>
                                        Access
                                    </th>

                                    <th>
                                        Uploaded
                                    </th>

                                    <th>
                                        Status
                                    </th>

                                    <th>
                                        Action
                                    </th>

                                </tr>

                            </thead>


                            <tbody>

                                {documents.map((document) => (

                                    <tr key={document.id}>


                                        {/* DOCUMENT */}

                                        <td>

                                            <div className="document-name">

                                                <span className="document-icon">
                                                    📄
                                                </span>

                                                <div>

                                                    <strong>
                                                        {document.title ||
                                                            document.fileName}
                                                    </strong>

                                                    <span>
                                                        {document.fileName}
                                                    </span>

                                                </div>

                                            </div>

                                        </td>


                                        {/* DEPARTMENT */}

                                        <td>
                                            {document.department ||
                                                'GENERAL'}
                                        </td>


                                        {/* VERSION */}

                                        <td>
                                            {document.version ||
                                                '1.0'}
                                        </td>


                                        {/* ACCESS */}

                                        <td>

                                            {document.accessLevel === 'PUBLIC'
                                                ? 'Public'
                                                : 'Employee Only'}

                                        </td>


                                        {/* UPLOADED */}

                                        <td>

                                            {document.uploadedAt
                                                ? new Date(
                                                    document.uploadedAt
                                                ).toLocaleDateString()
                                                : '—'}

                                        </td>


                                        {/* STATUS */}

                                        <td>

                                            <span className="status-badge active">
                                                Active
                                            </span>

                                        </td>


                                        {/* ACTION */}

                                        <td>

                                            <button
                                                className="deactivate-button"
                                                onClick={() =>
                                                    handleDeactivate(
                                                        document.id
                                                    )
                                                }
                                                disabled={
                                                    deactivatingId ===
                                                    document.id
                                                }
                                            >

                                                {deactivatingId ===
                                                    document.id
                                                    ? 'Deactivating...'
                                                    : 'Deactivate'}

                                            </button>

                                        </td>

                                    </tr>

                                ))}

                            </tbody>

                        </table>

                    </div>

                )}

            </section>


        </div>

    );

}


export default AdminDocuments;