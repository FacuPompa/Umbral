import { Skeleton } from '@/components/ui/skeleton';

export default function ModerationLoading() {
  return (
    <div role="status" aria-label="Cargando sugerencias pendientes" className="grid gap-10">
      <span className="sr-only">Cargando sugerencias pendientes</span>
      {[true, false].map((withCover) => (
        <div key={String(withCover)} aria-hidden="true" className="grid gap-5">
          <Skeleton className="h-7 w-52 max-w-full" />
          <div className="flex gap-4 border-t border-border py-6 sm:gap-6">
            {withCover && <Skeleton className="h-28 w-20 shrink-0" />}
            <div className="grid min-w-0 flex-1 gap-4">
              <Skeleton className="h-6 w-64 max-w-full" />
              <Skeleton className="h-4 w-40 max-w-full" />
              <Skeleton className={withCover ? 'h-24 w-full' : 'h-5 w-48 max-w-full'} />
              <div className="flex flex-wrap gap-3"><Skeleton className="h-11 w-36" /><Skeleton className="h-11 w-24" /></div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
