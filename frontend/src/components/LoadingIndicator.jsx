export default function LoadingIndicator({ label = 'Cargando contenido' }) {
  return (
    <span className="loading-indicator" aria-label={label} role="status">
      <span aria-hidden="true" className="loading-indicator-mark" />
    </span>
  );
}
