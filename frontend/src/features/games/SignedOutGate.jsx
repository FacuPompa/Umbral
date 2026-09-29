import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';

export default function SignedOutGate({ from }) {
  return (
    <section className="grid gap-5 border-y border-border bg-surface-subtle px-4 py-6 sm:p-6" aria-labelledby="conversation-access-title">
      <h2 id="conversation-access-title" className="text-2xl leading-[30px] font-semibold">Sumate a la conversación</h2>
      <p className="leading-6 text-muted-foreground">Iniciá sesión y marcá hasta dónde jugaste para leer, publicar y responder sobre lo que ya conocés.</p>
      <p className="text-sm leading-5 text-muted-foreground">Las publicaciones de tramos posteriores quedan fuera de tu lectura.</p>
      <div className="flex flex-wrap gap-3">
        <Button asChild><Link to="/register" state={{ from }}>Crear cuenta</Link></Button>
        <Button asChild variant="outline"><Link to="/login" state={{ from }}>Iniciar sesión</Link></Button>
      </div>
    </section>
  );
}
