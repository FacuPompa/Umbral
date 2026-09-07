import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from './useAuth';

export default function AuthPage({ mode }) {
  const isRegister = mode === 'register';
  const navigate = useNavigate();
  const location = useLocation();
  const { user, loading, login, register } = useAuth();
  const [form, setForm] = useState({ handle: '', email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const destination = location.state?.from?.pathname ?? '/';

  if (loading) {
    return <main className="page-state" id="main-content">Recuperando tu sesión...</main>;
  }

  if (user) {
    return <Navigate replace to={destination} />;
  }

  function updateField(event) {
    const { name, value } = event.target;
    setForm((currentForm) => ({ ...currentForm, [name]: value }));
  }

  async function submit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      if (isRegister) {
        await register(form);
      } else {
        await login(form);
      }
      navigate(destination, { replace: true });
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="auth-page" id="main-content">
      <section className="auth-form-section" aria-labelledby="auth-title">
        <header className="auth-heading">
          <h1 id="auth-title">{isRegister ? 'Creá tu cuenta' : 'Iniciá sesión'}</h1>
          <p>
            {isRegister
              ? 'Elegí un nombre para participar y guardar tu progreso.'
              : 'Volvé a tu progreso y a tus conversaciones seguras.'}
          </p>
        </header>

        <form className="auth-form" onSubmit={submit}>
          <label>
            Nombre de usuario
            <input
              autoComplete="username"
              maxLength="30"
              minLength="3"
              name="handle"
              onChange={updateField}
              pattern="[A-Za-z0-9_-]+"
              required
              value={form.handle}
            />
            {isRegister && <span>Usá entre 3 y 30 caracteres: letras, números, guiones o guion bajo.</span>}
          </label>

          {isRegister && (
            <label>
              Email
              <input
                autoComplete="email"
                maxLength="254"
                name="email"
                onChange={updateField}
                required
                type="email"
                value={form.email}
              />
            </label>
          )}

          <label>
            Contraseña
            <span className="password-field">
              <input
                autoComplete={isRegister ? 'new-password' : 'current-password'}
                maxLength="72"
                minLength="12"
                name="password"
                onChange={updateField}
                required
                type={showPassword ? 'text' : 'password'}
                value={form.password}
              />
              <button
                aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                aria-pressed={showPassword}
                className="password-toggle"
                onClick={() => setShowPassword((currentValue) => !currentValue)}
                type="button"
              >
                {showPassword ? 'Ocultar' : 'Mostrar'}
              </button>
            </span>
            {isRegister && <span>Usá al menos 12 caracteres.</span>}
          </label>

          {error && <p className="form-error" role="alert">{error}</p>}
          <button className="button-primary" disabled={submitting} type="submit">
            {submitting ? 'Guardando...' : isRegister ? 'Crear cuenta' : 'Iniciar sesión'}
          </button>
        </form>

        <p className="auth-switch">
          {isRegister ? '¿Ya tenés cuenta?' : '¿Todavía no tenés cuenta?'}{' '}
          <Link to={isRegister ? '/login' : '/register'}>
            {isRegister ? 'Iniciá sesión' : 'Creá una cuenta'}
          </Link>
        </p>
      </section>
    </main>
  );
}
