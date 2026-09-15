import { cn } from '@/lib/utils';

export default function InitialAvatar({ handle, className }) {
  return (
    <span aria-hidden="true" className={cn('inline-grid size-8 shrink-0 place-items-center rounded-full border border-border bg-muted text-sm font-semibold text-foreground', className)}>
      {handle.slice(0, 1).toUpperCase()}
    </span>
  );
}
