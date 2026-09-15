import { Slot } from '@radix-ui/react-slot';
import { cva } from 'class-variance-authority';
import { cn } from '@/lib/utils';

const variants = cva(
  'inline-flex min-h-11 shrink-0 items-center justify-center gap-2 rounded-md px-4 text-sm font-medium no-underline transition-colors duration-150 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring disabled:pointer-events-none disabled:opacity-50 [&_svg]:size-[18px] [&_svg]:shrink-0',
  {
    variants: {
      variant: {
        primary: 'bg-primary text-primary-foreground hover:bg-[var(--accent-hover)]',
        outline: 'border border-input bg-transparent text-foreground hover:bg-accent',
        ghost: 'bg-transparent text-foreground hover:bg-accent',
        destructive: 'bg-transparent text-destructive hover:bg-accent',
      },
      size: { default: '', icon: 'size-11 p-0' },
    },
    defaultVariants: { variant: 'primary', size: 'default' },
  },
);

export function Button({ asChild = false, variant, size, className, ...props }) {
  const Component = asChild ? Slot : 'button';
  return <Component data-slot="button" className={cn(variants({ variant, size }), className)} {...props} />;
}
