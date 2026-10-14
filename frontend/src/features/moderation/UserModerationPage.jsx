import { useEffect, useRef, useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';
import { fetchModeratedUsers, reactivateUser, suspendUser } from './userModerationApi';
import PageHeading from '../../components/PageHeading';
import StatusMessage from '../../components/StatusMessage';
import LoadingIndicator from '../../components/LoadingIndicator';
import ConfirmDialog from '../../components/ConfirmDialog';
import InitialAvatar from '../../components/InitialAvatar';
import { Field, Input, Textarea } from '../../components/ui/field';
import { Button } from '../../components/ui/button';
import { Skeleton } from '../../components/ui/skeleton';

const dateFormatter = new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium', timeStyle: 'short' });

export default function UserModerationPage() {
  const { user, loading: loadingSession } = useAuth();
  const [query, setQuery] = useState('');
  const [filters, setFilters] = useState({ query: '', status: '', page: 0 });
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [target, setTarget] = useState(null);
  const [reason, setReason] = useState('');
  const [working, setWorking] = useState(false);
  const [actionError, setActionError] = useState('');
  const [success, setSuccess] = useState('');
  const refreshRef = useRef(null);

  useEffect(() => {
    if (!user || user.role !== 'MODERATOR') return undefined;
    let active = true;
    async function load() {
      setLoading(true);
      setError('');
      try {
        const data = await fetchModeratedUsers(filters);
        if (active) setResult(data);
      } catch (requestError) {
        if (active) setError(requestError.message);
      } finally {
        if (active) setLoading(false);
      }
    }
    load();
    return () => { active = false; };
  }, [user, filters, reloadKey]);

  if (loadingSession) return <main id="main-content" className="py-12"><LoadingIndicator label="Recuperando sesión" showLabel /></main>;
  if (!user) return <Navigate replace to="/login" />;
  if (user.role !== 'MODERATOR') return <Navigate replace to="/" />;

  function openAction(account) {
    setTarget(account);
    setReason('');
    setActionError('');
  }

  async function confirmAction() {
    if (!target || working) return;
    setWorking(true);
    setActionError('');
    try {
      if (target.suspended) await reactivateUser(target.id);
      else await suspendUser(target.id, reason.trim());
      setSuccess(target.suspended
        ? `Se reactivó la cuenta @${target.handle}. Debe iniciar sesión nuevamente.`
        : `Se suspendió la cuenta @${target.handle}.`);
      setTarget(null);
      setReloadKey((key) => key + 1);
    } catch (requestError) {
      setActionError(requestError.message);
    } finally {
      setWorking(false);
    }
  }

  return (
    <main id="main-content" className="mx-auto grid w-full max-w-[900px] gap-8 py-8 md:py-12">
      <PageHeading title="Moderación de usuarios" description="Suspendé o reactivá cuentas sin borrar sus publicaciones, biblioteca ni progreso." />
      <nav aria-label="Secciones de moderación" className="flex flex-wrap gap-4 text-sm">
        <Link className="underline underline-offset-4" to="/moderation/games">Juegos y checkpoints</Link>
        <span aria-current="page" className="font-semibold">Usuarios</span>
      </nav>
      <form className="grid gap-4 sm:grid-cols-[minmax(0,1fr)_180px_auto] sm:items-end" onSubmit={(event) => {
        event.preventDefault();
        setFilters((current) => ({ ...current, query: query.trim(), page: 0 }));
      }}>
        <Field id="moderation-query" label="Nombre de usuario">
          <Input id="moderation-query" maxLength={30} value={query} onChange={(event) => setQuery(event.target.value)} autoComplete="off" />
        </Field>
        <Field id="moderation-status" label="Estado">
          <select id="moderation-status" className="min-h-11 rounded-md border border-input bg-popover px-3 text-base"
            value={filters.status} onChange={(event) => setFilters((current) => ({ ...current, status: event.target.value, page: 0 }))}>
            <option value="">Todos</option><option value="active">Activos</option><option value="suspended">Suspendidos</option>
          </select>
        </Field>
        <Button type="submit" disabled={loading}>Buscar</Button>
      </form>
      {success && <StatusMessage kind="success">{success}</StatusMessage>}
      <section className="grid gap-4" aria-labelledby="moderated-users-title" aria-busy={loading}>
        <header className="flex flex-wrap items-center justify-between gap-3">
          <h2 ref={refreshRef} tabIndex={-1} id="moderated-users-title" className="text-2xl font-semibold">Usuarios</h2>
          <Button type="button" variant="outline" disabled={loading}
            onClick={() => setReloadKey((key) => key + 1)}>Actualizar</Button>
        </header>
        {loading ? <div className="grid gap-4" role="status" aria-label="Cargando usuarios">{[0, 1, 2].map((key) => <Skeleton key={key} className="h-24 w-full" />)}</div>
          : error ? <StatusMessage kind="error">{error} Podés reintentar con Actualizar.</StatusMessage>
            : result?.items.length === 0 ? <p className="text-muted-foreground">No hay usuarios que coincidan con la búsqueda.</p>
              : <ul className="divide-y divide-border border-y border-border">
                {result?.items.map((account) => (
                  <li key={account.id} className="grid min-w-0 gap-4 py-5 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-start">
                    <div className="flex min-w-0 items-start gap-3">
                      <InitialAvatar handle={account.handle} />
                      <div className="grid min-w-0 gap-2">
                        <h3 className="break-all font-semibold">@{account.handle}</h3>
                        <p className="text-sm">{account.suspended ? 'Cuenta suspendida' : 'Cuenta activa'}{account.role === 'MODERATOR' ? ' · Moderador' : ''}</p>
                        {account.suspensionReason && <p className="whitespace-pre-wrap break-words text-sm text-muted-foreground">Motivo: {account.suspensionReason}</p>}
                        {account.statusChangedAt && <p className="break-words text-sm text-muted-foreground">Último cambio por @{account.statusChangedByHandle} · <time dateTime={account.statusChangedAt}>{dateFormatter.format(new Date(account.statusChangedAt))}</time></p>}
                      </div>
                    </div>
                    {account.role === 'MODERATOR' || account.id === user.id
                      ? <span className="text-sm text-muted-foreground">Cuenta protegida</span>
                      : <Button type="button" variant="outline" className="justify-self-start" onClick={() => openAction(account)}>
                        {account.suspended ? 'Reactivar' : 'Suspender'}<span className="sr-only"> a @{account.handle}</span>
                      </Button>}
                  </li>
                ))}
              </ul>}
        <nav aria-label="Páginas de usuarios" className="flex flex-wrap items-center gap-3">
          <Button variant="outline" disabled={loading || filters.page === 0} onClick={() => setFilters((current) => ({ ...current, page: current.page - 1 }))}>Anterior</Button>
          <span className="text-sm text-muted-foreground" aria-live="polite">Página {filters.page + 1}</span>
          <Button variant="outline" disabled={loading || Boolean(error) || !result?.hasNext} onClick={() => setFilters((current) => ({ ...current, page: current.page + 1 }))}>Siguiente</Button>
        </nav>
      </section>
      <ConfirmDialog open={Boolean(target)} onOpenChange={(open) => { if (!open) setTarget(null); }}
        title={target?.suspended ? `Reactivar a @${target.handle}` : `Suspender a @${target?.handle ?? ''}`}
        description={target?.suspended
          ? 'La persona podrá volver a iniciar sesión. Las sesiones anteriores seguirán invalidadas.'
          : 'La cuenta dejará de poder iniciar sesión y sus sesiones anteriores quedarán invalidadas. Su contenido se conserva.'}
        confirmLabel={target?.suspended ? 'Reactivar cuenta' : 'Suspender cuenta'}
        pendingLabel="Guardando cambio" busy={working} error={actionError} onConfirm={confirmAction}
        destructive={!target?.suspended} confirmDisabled={!target?.suspended && reason.trim().length < 3} fallbackFocusRef={refreshRef}>
        {target && !target.suspended && <Field id="suspension-reason" label="Motivo de suspensión" hint="Entre 3 y 500 caracteres. Solo lo ve moderación.">
          <Textarea id="suspension-reason" aria-describedby="suspension-reason-hint" maxLength={500} value={reason}
            disabled={working} onChange={(event) => setReason(event.target.value)} />
        </Field>}
      </ConfirmDialog>
    </main>
  );
}
