// Adaptación en JavaScript del patrón shadcn/ui sobre Radix.
// Motion controla la presencia; Radix conserva teclado, foco y selección.
import * as DropdownPrimitive from '@radix-ui/react-dropdown-menu';
import { AnimatePresence, m as motion, useReducedMotion } from 'motion/react';
import { cn } from '@/lib/utils';

export const DropdownMenu = DropdownPrimitive.Root;
export const DropdownMenuTrigger = DropdownPrimitive.Trigger;
export const DropdownMenuLabel = DropdownPrimitive.Label;
export const DropdownMenuSeparator = DropdownPrimitive.Separator;

export function DropdownMenuContent({ open, className, children, ...props }) {
  const reducedMotion = useReducedMotion();
  return (
    <DropdownPrimitive.Portal forceMount>
      <AnimatePresence>
        {open && (
          <DropdownPrimitive.Content forceMount asChild align="end" sideOffset={8} collisionPadding={12} {...props}>
            <motion.div
              initial={{ opacity: reducedMotion ? 1 : 0 }}
              animate={{ opacity: 1, transition: { duration: reducedMotion ? 0 : 0.12 } }}
              exit={{ opacity: 0, transition: { duration: reducedMotion ? 0 : 0.1 } }}
              className={cn('z-50 w-60 max-w-[calc(100vw-24px)] rounded-lg border border-border bg-popover p-1 text-popover-foreground shadow-overlay', className)}
            >
              {children}
            </motion.div>
          </DropdownPrimitive.Content>
        )}
      </AnimatePresence>
    </DropdownPrimitive.Portal>
  );
}

export function DropdownMenuItem({ className, ...props }) {
  return <DropdownPrimitive.Item className={cn('flex min-h-11 cursor-pointer items-center gap-3 rounded-md px-3 py-2 text-sm outline-none select-none data-[highlighted]:bg-accent data-[disabled]:pointer-events-none data-[disabled]:opacity-50 [&_svg]:size-[18px] [&_svg]:shrink-0', className)} {...props} />;
}
