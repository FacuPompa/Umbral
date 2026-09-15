import { LoaderCircle } from 'lucide-react';
import { cn } from '@/lib/utils';

export default function LoadingIndicator({ label = 'Cargando contenido', showLabel = false, className }) {
  return (
    <span className={cn('inline-flex items-center gap-3 text-sm leading-5', className)} role="status">
      <LoaderCircle aria-hidden="true" className="size-5 shrink-0 animate-spin motion-reduce:animate-none" />
      <span className={showLabel ? '' : 'sr-only'}>{label}</span>
    </span>
  );
}
