import { Skeleton } from './ui/skeleton';

export default function ContentLoading({ label, variant = 'rows', showIdentity = false }) {
  return (
    <div role="status" aria-label={label} className="grid min-w-0 gap-8">
      <span className="sr-only">{label}</span>
      <div aria-hidden="true" className="grid min-w-0 gap-8">
        {showIdentity && (
          <div className="flex items-center gap-4">
            <Skeleton className="size-16 shrink-0 rounded-full sm:size-20" />
            <Skeleton className="h-8 w-48 max-w-full" />
          </div>
        )}
        {variant === 'profile' && (
          <div className="grid grid-cols-3 gap-4 border-y border-border py-6">
            {[0, 1, 2].map((item) => <div key={item} className="grid gap-3"><Skeleton className="h-8 w-12" /><Skeleton className="h-4 w-20 max-w-full" /></div>)}
          </div>
        )}
        {variant === 'rows' ? (
          <div className="grid divide-y divide-border border-y border-border">
            {[0, 1].map((item) => (
              <div key={item} className="flex min-w-0 gap-4 py-6">
                <Skeleton className="h-28 w-20 shrink-0" />
                <div className="grid min-w-0 flex-1 grid-cols-[minmax(0,1fr)] content-start gap-4">
                  <Skeleton className="h-6 w-60 max-w-full" />
                  <Skeleton className="h-4 w-40 max-w-full" />
                  <Skeleton className="h-4 w-52 max-w-full" />
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className={`grid min-w-0 gap-4 sm:gap-6 ${variant === 'profile' ? 'grid-cols-2 sm:grid-cols-3' : 'grid-cols-1 md:grid-cols-3'}`}>
            {[0, 1, 2].map((item) => (
              <div key={item} className={`grid min-w-0 gap-3 ${variant === 'shelf' && item > 0 ? 'hidden md:grid' : ''}`}>
                <Skeleton className={variant === 'profile' ? 'aspect-[3/4] w-full' : 'aspect-[4/3] w-full'} />
                <Skeleton className="h-5 w-32 max-w-full" />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
