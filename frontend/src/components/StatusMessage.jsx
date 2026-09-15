import { Check, TriangleAlert } from 'lucide-react';
import { cn } from '@/lib/utils';

export default function StatusMessage({ kind = 'info', children, className }) {
  const Icon = kind === 'error' ? TriangleAlert : kind === 'success' ? Check : null;
  return (
    <div role={kind === 'error' ? 'alert' : 'status'} className={cn('flex items-start gap-3 border-l-2 border-border py-2 pl-4 text-base leading-6 text-muted-foreground', kind === 'error' && 'border-destructive text-destructive', kind === 'success' && 'border-[var(--success)] text-[var(--success)]', className)}>
      {Icon && <Icon aria-hidden="true" className="mt-0.5 size-5 shrink-0" />}
      <div className="min-w-0 break-words">{children}</div>
    </div>
  );
}
