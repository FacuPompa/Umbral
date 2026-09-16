import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom';
import { BookOpen, Search, Menu, Sun, Moon, UserRound, Library, ChevronDown, LogOut, Plus, ShieldCheck, ExternalLink, LoaderCircle, TriangleAlert } from 'lucide-react';
import { useAuth } from '../features/auth/useAuth';
import InitialAvatar from '@/components/InitialAvatar';
import { Button } from '@/components/ui/button';
import { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator } from '@/components/ui/dropdown-menu';
import { Sheet, SheetTrigger, SheetContent, SheetTitle, SheetDescription, SheetClose } from '@/components/ui/sheet';

function getInitialTheme() {
  return document.documentElement.dataset.theme === 'light' ? 'light' : 'dark';
}

function accountLinks(user) {
  return [
    { label: 'Mi perfil', to: '/me', icon: UserRound },
    { label: 'Mi biblioteca', to: '/me/library', icon: Library },
    { label: 'Ver perfil público', to: `/users/${encodeURIComponent(user.handle)}`, icon: ExternalLink },
    { label: 'Sugerir juego', to: '/suggestions/new', icon: Plus },
    ...(user.role === 'MODERATOR' ? [{ label: 'Moderación', to: '/moderation/games', icon: ShieldCheck }] : []),
  ];
}

function LogoutError({ message }) {
  return message && (
    <p className="flex items-start gap-2 rounded-md border border-destructive p-3 text-sm leading-5 text-destructive" role="alert">
      <TriangleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
      {message}
    </p>
  );
}

export default function AppShell() {
  const [theme, setTheme] = useState(getInitialTheme);
  const [mobileRoute, setMobileRoute] = useState(null);
  const [profileRoute, setProfileRoute] = useState(null);
  const [loggingOut, setLoggingOut] = useState(false);
  const [logoutError, setLogoutError] = useState(null);
  const { user, loading, logout } = useAuth();
  const location = useLocation();
  const brandRef = useRef(null);
  const mobileOpen = mobileRoute === location.key;
  const profileOpen = profileRoute === location.key;

  // También olvidar el menú al usar Atrás/Adelante, para no reabrirlo al
  // volver a una entrada del historial que conserva su location.key.
  if (mobileRoute !== null && mobileRoute !== location.key) setMobileRoute(null);
  if (profileRoute !== null && profileRoute !== location.key) setProfileRoute(null);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    try { window.localStorage.setItem('umbral-theme', theme); } catch { /* El tema sigue funcionando sin persistencia. */ }
  }, [theme]);

  useEffect(() => {
    const desktop = window.matchMedia('(min-width: 768px)');
    function closeOverlays() {
      setMobileRoute(null);
      setProfileRoute(null);
    }
    desktop.addEventListener('change', closeOverlays);
    return () => desktop.removeEventListener('change', closeOverlays);
  }, []);

  async function handleLogout() {
    if (loggingOut) return;
    setLoggingOut(true);
    setLogoutError(null);
    try {
      await logout();
      setMobileRoute(null);
      setProfileRoute(null);
      brandRef.current?.focus();
    } catch {
      setLogoutError('No se pudo cerrar la sesión. Probá de nuevo.');
    } finally {
      setLoggingOut(false);
    }
  }

  const themeLabel = theme === 'dark' ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro';
  const ThemeIcon = theme === 'dark' ? Sun : Moon;
  const LogoutIcon = loggingOut ? LoaderCircle : LogOut;
  const options = user ? accountLinks(user) : [];
  const navLinkClass = 'inline-flex min-h-11 items-center gap-2 rounded-md px-3 text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-foreground aria-[current=page]:text-foreground';

  return (
    <div className="mx-auto w-[calc(100%-32px)] max-w-[1180px] md:w-[calc(100%-48px)]">
      <a className="fixed -top-20 left-4 z-[60] rounded-md bg-primary px-4 py-3 font-medium text-primary-foreground focus:top-3" href="#main-content">Saltar al contenido</a>
      <header className="flex min-h-[60px] items-center gap-2 border-b border-border md:min-h-20 md:gap-8">
        <Link ref={brandRef} className="inline-flex min-h-11 shrink-0 items-center gap-2.5 rounded-md pr-2 text-xl font-semibold tracking-[-0.02em] md:text-2xl" to="/" aria-label="Umbral, inicio">
          <img alt="" src="/favicon.svg" className="size-6 md:size-7" />
          <span>Umbral</span>
        </Link>

        <nav className="hidden items-center gap-2 md:flex" aria-label="Navegación principal">
          <a className={navLinkClass} href="/#catalogo">Catálogo</a>
          <NavLink className={navLinkClass} to="/search"><Search aria-hidden="true" className="size-[18px]" />Buscar</NavLink>
        </nav>

        <div className="ml-auto flex items-center gap-0 md:gap-3">
          <Button asChild variant="ghost" size="icon" className="md:hidden">
            <Link to="/search" aria-label="Buscar juegos y personas"><Search aria-hidden="true" /></Link>
          </Button>
          <Button variant="ghost" size="icon" aria-label={themeLabel} title={themeLabel} onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}>
            <ThemeIcon aria-hidden="true" />
          </Button>

          <div className="hidden min-w-44 justify-end md:flex">
            {loading ? (
              <span role="status" aria-label="Recuperando sesión" className="inline-flex h-11 w-44 items-center justify-end">
                <span aria-hidden="true" className="h-8 w-32 rounded-md bg-muted" />
              </span>
            ) : user ? (
              <DropdownMenu open={profileOpen} onOpenChange={(open) => setProfileRoute(open ? location.key : null)}>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" aria-label={`Abrir menú de ${user.handle}`} className="gap-2 px-2">
                    <InitialAvatar handle={user.handle} />
                    <span className="max-w-36 truncate">{user.handle}</span>
                    <ChevronDown aria-hidden="true" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent open={profileOpen}>
                  <DropdownMenuLabel className="break-all px-3 py-3 text-sm font-semibold">{user.handle}</DropdownMenuLabel>
                  <DropdownMenuSeparator className="my-1 h-px bg-border" />
                  {options.map(({ label, to, icon: Icon }) => (
                    <DropdownMenuItem asChild key={to}>
                      <Link to={to} onClick={() => setProfileRoute(null)}><Icon aria-hidden="true" />{label}</Link>
                    </DropdownMenuItem>
                  ))}
                  <DropdownMenuSeparator className="my-1 h-px bg-border" />
                  <DropdownMenuItem disabled={loggingOut} onSelect={(event) => { event.preventDefault(); handleLogout(); }}>
                    <LogoutIcon aria-hidden="true" className={loggingOut ? 'animate-spin motion-reduce:animate-none' : ''} />
                    {loggingOut ? 'Cerrando sesión…' : 'Cerrar sesión'}
                  </DropdownMenuItem>
                  {logoutError && <div className="p-2"><LogoutError message={logoutError} /></div>}
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <div className="flex items-center gap-2">
                <Button asChild variant="ghost"><Link to="/login">Iniciar sesión</Link></Button>
                <Button asChild><Link to="/register">Crear cuenta</Link></Button>
              </div>
            )}
          </div>

          <Sheet open={mobileOpen} onOpenChange={(open) => setMobileRoute(open ? location.key : null)}>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon" className="md:hidden" aria-label="Abrir menú"><Menu aria-hidden="true" /></Button>
            </SheetTrigger>
            <SheetContent open={mobileOpen}>
              <SheetTitle className="pr-10 text-xl font-semibold">Umbral</SheetTitle>
              <SheetDescription className="sr-only">Navegación y opciones de cuenta</SheetDescription>
              <nav className="mt-6 flex flex-col gap-1 border-b border-border pb-6" aria-label="Navegación móvil">
                <SheetClose asChild><a className={navLinkClass} href="/#catalogo"><BookOpen aria-hidden="true" className="size-[18px]" />Catálogo</a></SheetClose>
                <SheetClose asChild><Link className={navLinkClass} to="/search"><Search aria-hidden="true" className="size-[18px]" />Buscar</Link></SheetClose>
              </nav>
              {loading ? (
                <p role="status" className="py-6 text-sm text-muted-foreground">Recuperando sesión…</p>
              ) : user ? (
                <>
                  <div className="flex min-w-0 items-center gap-3 py-6">
                    <InitialAvatar handle={user.handle} className="size-10" />
                    <span className="min-w-0 break-all text-base font-semibold">{user.handle}</span>
                  </div>
                  <nav className="flex flex-col gap-1" aria-label="Mi cuenta">
                    {options.map(({ label, to, icon: Icon }) => (
                      <SheetClose asChild key={to}><Link to={to} className={navLinkClass}><Icon aria-hidden="true" className="size-[18px]" />{label}</Link></SheetClose>
                    ))}
                  </nav>
                  <div className="mt-6 grid gap-3 border-t border-border pt-4">
                    <Button variant="ghost" className="justify-start px-3" disabled={loggingOut} onClick={handleLogout}>
                      <LogoutIcon aria-hidden="true" className={loggingOut ? 'animate-spin motion-reduce:animate-none' : ''} />
                      {loggingOut ? 'Cerrando sesión…' : 'Cerrar sesión'}
                    </Button>
                    <LogoutError message={logoutError} />
                  </div>
                </>
              ) : (
                <div className="mt-6 grid gap-3">
                  <SheetClose asChild><Button asChild><Link to="/register">Crear cuenta</Link></Button></SheetClose>
                  <SheetClose asChild><Button asChild variant="outline"><Link to="/login">Iniciar sesión</Link></Button></SheetClose>
                </div>
              )}
            </SheetContent>
          </Sheet>
        </div>
      </header>
      {logoutError && !mobileOpen && !profileOpen && <div className="pt-4"><LogoutError message={logoutError} /></div>}
      <Outlet />
    </div>
  );
}
