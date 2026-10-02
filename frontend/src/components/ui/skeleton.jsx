import { cn } from '@/lib/utils';

export function Skeleton({ className }) {
  return <span aria-hidden="true" className={cn('block rounded-md bg-muted motion-safe:animate-pulse', className)} />;
}
