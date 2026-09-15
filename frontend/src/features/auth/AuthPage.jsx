import { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Field, Input } from '@/components/ui/field';
import StatusMessage from '@/components/StatusMessage';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import LoadingIndicator from '../../components/LoadingIndicator';
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

  const from = location.state?.from;
  const destination = from ? `${from.pathname}${from.search ?? ''}${from.hash ?? ''}` : '/';

  if (loading) {
    return <main className="py-12" id="main-content"><LoadingIndicator label="Recuperando sesión" showLabel /></main>;
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
    if (submitting) return;
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
    <main className="mx-auto w-full max-w-[460px] py-12 md:py-16" id="main-content">
      <section className="grid gap-8" aria-labelledby="auth-title">
        <header className="grid gap-3">
          <h1 className="text-[28px] leading-[34px] font-semibold tracking-[-0.02em] md:text-4xl md:leading-[42px]" id="auth-title">{isRegister ? 'Creá tu cuenta' : 'Iniciá sesión'}</h1>
          <p className="text-base leading-6 text-muted-foreground">
            {isRegister
              ? 'Elegí un nombre para participar y guardar tu progreso.'
              : 'Volvé a tu progreso y a tus conversaciones seguras.'}
          </p>
        </header>

        <form className="grid gap-6 border-t border-border pt-8" onSubmit={submit} aria-busy={submitting}>
          <Field id="auth-handle" label="Nombre de usuario" hint={isRegister ? 'Usá entre 3 y 30 caracteres: letras, números, guiones o guion bajo.' : undefined}>
            <Input
              id="auth-handle" autoComplete="username"
              maxLength={30} minLength={3} name="handle"
              onChange={updateField} pattern="[A-Za-z0-9_-]+" required
              value={form.handle} disabled={submitting}
              aria-describedby={isRegister ? 'auth-handle-hint' : undefined}
            />
          </Field>

          {isRegister && (
            <Field id="auth-email" label="Email">
              <Input id="auth-email" autoComplete="email" maxLength={254} name="email"
                onChange={updateField} required type="email" value={form.email} disabled={submitting} />
            </Field>
          )}

          <Field id="auth-password" label="Contraseña" hint={isRegister ? 'Usá al menos 8 caracteres.' : undefined}>
            <div className="relative">
              <Input
                id="auth-password" className="pr-12"
                autoComplete={isRegister ? 'new-password' : 'current-password'}
                maxLength={72} minLength={8} name="password"
                onChange={updateField} required disabled={submitting}
                type={showPassword ? 'text' : 'password'} value={form.password}
                aria-describedby={isRegister ? 'auth-password-hint' : undefined}
              />
              <Button variant="ghost" size="icon" className="absolute top-0.5 right-0.5"
                aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                aria-pressed={showPassword}
                onClick={() => setShowPassword((currentValue) => !currentValue)} type="button">
                {showPassword ? <EyeOff aria-hidden="true" /> : <Eye aria-hidden="true" />}
              </Button>
            </div>
          </Field>

          {error && <StatusMessage kind="error">{error}</StatusMessage>}
          <Button className="w-full text-base" disabled={submitting} type="submit">
            {submitting ? <LoadingIndicator label={isRegister ? 'Creando cuenta' : 'Iniciando sesión'} showLabel /> : isRegister ? 'Crear cuenta' : 'Iniciar sesión'}
          </Button>
        </form>

        <p className="text-sm leading-6 text-muted-foreground">
          {isRegister ? '¿Ya tenés cuenta?' : '¿Todavía no tenés cuenta?'}{' '}
          <Link className="font-medium text-foreground underline underline-offset-4 hover:text-primary" state={location.state} to={isRegister ? '/login' : '/register'}>
            {isRegister ? 'Iniciá sesión' : 'Creá una cuenta'}
          </Link>
        </p>
      </section>
    </main>
  );
}
