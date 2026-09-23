import { useTheme } from '../context/ThemeContext';

function ThemeToggle() {
  const { theme, toggleTheme } = useTheme();

  return (
    <button
      type="button"
      className="theme-toggle"
      onClick={toggleTheme}
      aria-label={
        theme === 'light'
          ? 'Switch to dark mode'
          : 'Switch to light mode'
      }
      title={
        theme === 'light'
          ? 'Switch to dark mode'
          : 'Switch to light mode'
      }
    >
      <span className="theme-toggle-icon">
        {theme === 'light' ? '🌙' : '☀️'}
      </span>

      <span className="theme-toggle-text">
        {theme === 'light'
          ? 'Dark mode'
          : 'Light mode'}
      </span>
    </button>
  );
}

export default ThemeToggle;