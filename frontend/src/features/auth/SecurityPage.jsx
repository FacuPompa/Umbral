import { useRef, useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import { useAuth } from './useAuth';
import PageHeading from '../../components/PageHeading';
import StatusMessage from '../../components/StatusMessage';
import LoadingIndicator from '../../components/LoadingIndicator';
import { Button } from '../../components/ui/button';
import { Field, Input } from '../../components/ui/field';

export default function SecurityPage() {
  const { user, loading, changePassword } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmation: '' });
  const [visible, setVisible] = useState({});
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const errorRef = useRef(null);
  if (loading) return <main id="main-content" className="py-12"><LoadingIndicator label="Recuperando sesión" showLabel /></main>;
  if (!user) return <Navigate replace to="/login" />;

  function showError(message) {
    setError(message);
    requestAnimationFrame(() => errorRef.current?.focus());
  }

  async function submit(event) {
    event.preventDefault();
    if (busy) return;
    setError('');
    if (form.newPassword !== form.confirmation) return showError('Las contraseñas nuevas no coinciden.');
    if (form.newPassword === form.currentPassword) return showError('Elegí una contraseña diferente de la actual.');
    if (new TextEncoder().encode(form.newPassword).length > 72) return showError('La nueva contraseña supera el límite de 72 bytes. Usá menos caracteres.');
    setBusy(true);
    try {
      await changePassword(form.currentPassword, form.newPassword);
      setForm({ currentPassword: '', newPassword: '', confirmation: '' });
      navigate('/login', { replace: true, state: { passwordChanged: true } });
    } catch (requestError) {
      showError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  const fields = [
    { name: 'currentPassword', label: 'Contraseña actual', autoComplete: 'current-password', maxLength: 256 },
    { name: 'newPassword', label: 'Nueva contraseña', autoComplete: 'new-password', minLength: 8, maxLength: 72,
      hint: 'Al menos 8 caracteres. Límite de 72 bytes: los emojis y algunos caracteres ocupan más de uno.' },
    { name: 'confirmation', label: 'Confirmar nueva contraseña', autoComplete: 'new-password', minLength: 8, maxLength: 72 },
  ];
  return (
    <main id="main-content" className="mx-auto grid w-full max-w-[620px] gap-8 py-8 md:py-12">
      <PageHeading title="Seguridad de la cuenta" description="Cambiá tu contraseña. Al guardar, se cerrará esta sesión y se invalidarán las de otros dispositivos." />
      <Link className="justify-self-start text-sm underline underline-offset-4" to="/me">Volver a mi perfil</Link>
      <form className="grid gap-6 border-t border-border pt-6" aria-busy={busy} onSubmit={submit}>
        {fields.map(({ name, label, hint, ...props }) => (
          <Field key={name} id={`security-${name}`} label={label} hint={hint}>
            <div className="relative">
              <Input id={`security-${name}`} name={name} type={visible[name] ? 'text' : 'password'} required disabled={busy}
                value={form[name]} {...props} className="pr-14" aria-describedby={hint ? `security-${name}-hint` : undefined}
                onChange={(event) => setForm((current) => ({ ...current, [name]: event.target.value }))} />
              <Button type="button" variant="ghost" size="icon" className="absolute right-0 top-0" disabled={busy}
                aria-label={`${visible[name] ? 'Ocultar' : 'Mostrar'} ${label.toLowerCase()}`} aria-pressed={Boolean(visible[name])}
                onClick={() => setVisible((current) => ({ ...current, [name]: !current[name] }))}>
                {visible[name] ? <EyeOff aria-hidden="true" /> : <Eye aria-hidden="true" />}
              </Button>
            </div>
          </Field>
        ))}
        {error && <div ref={errorRef} tabIndex={-1}><StatusMessage kind="error">{error}</StatusMessage></div>}
        <Button type="submit" disabled={busy} className="justify-self-start">{busy ? <LoadingIndicator label="Guardando contraseña" showLabel /> : 'Cambiar contraseña y cerrar sesiones'}</Button>
      </form>
    </main>
  );
}
