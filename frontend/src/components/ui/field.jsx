import { cn } from '@/lib/utils';

export function Input({ className, ...props }) {
  return <input className={cn('min-h-11 w-full min-w-0 rounded-md border border-input bg-popover px-3 py-2 text-base leading-6 text-foreground placeholder:text-muted-foreground focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring disabled:cursor-not-allowed disabled:opacity-60 aria-invalid:border-destructive', className)} {...props} />;
}

export function Textarea({ className, ...props }) {
  return <textarea className={cn('min-h-32 w-full resize-y rounded-md border border-input bg-popover px-3 py-2 text-base leading-6 text-foreground placeholder:text-muted-foreground focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring disabled:opacity-60 aria-invalid:border-destructive', className)} {...props} />;
}

// Asociar hint/error al control con aria-describedby: `${id}-hint` / `${id}-error`.
export function Field({ id, label, hint, error, children, className }) {
  return (
    <div className={cn('grid gap-2', className)}>
      <label htmlFor={id} className="text-base font-medium leading-6 text-foreground">{label}</label>
      {children}
      {hint && <p id={`${id}-hint`} className="text-sm leading-5 text-muted-foreground">{hint}</p>}
      {error && <p id={`${id}-error`} className="text-sm leading-5 text-destructive">{error}</p>}
    </div>
  );
}
