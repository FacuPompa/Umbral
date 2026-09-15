// Adaptación del Sheet de shadcn/ui: Dialog de Radix con transición de Motion.
import * as DialogPrimitive from '@radix-ui/react-dialog';
import { AnimatePresence, m as motion, useReducedMotion } from 'motion/react';
import { X } from 'lucide-react';
import { Button } from './button';

export const Sheet = DialogPrimitive.Root;
export const SheetTrigger = DialogPrimitive.Trigger;
export const SheetClose = DialogPrimitive.Close;
export const SheetTitle = DialogPrimitive.Title;
export const SheetDescription = DialogPrimitive.Description;

export function SheetContent({ open, children, ...props }) {
  const reducedMotion = useReducedMotion();
  const duration = reducedMotion ? 0 : 0.18;
  const exitDuration = reducedMotion ? 0 : 0.14;

  return (
    <DialogPrimitive.Portal forceMount>
      <AnimatePresence>
        {open && (
          <DialogPrimitive.Overlay key="overlay" forceMount asChild>
            <motion.div
              className="fixed inset-0 z-50 bg-black/40"
              initial={{ opacity: reducedMotion ? 1 : 0 }} animate={{ opacity: 1, transition: { duration } }}
              exit={{ opacity: 0, transition: { duration: exitDuration } }}
            />
          </DialogPrimitive.Overlay>
        )}
        {open && (
          <DialogPrimitive.Content key="content" forceMount asChild {...props}>
            <motion.div
              className="fixed inset-y-0 right-0 z-50 flex w-[min(360px,calc(100%-24px))] flex-col overflow-y-auto overscroll-contain border-l border-border bg-popover px-6 pt-6 pb-[max(24px,env(safe-area-inset-bottom))] text-popover-foreground shadow-overlay"
              initial={{ x: reducedMotion ? 0 : '100%' }} animate={{ x: 0, transition: { duration } }}
              exit={{ x: reducedMotion ? 0 : '100%', transition: { duration: exitDuration } }}
            >
              <DialogPrimitive.Close asChild>
                <Button variant="ghost" size="icon" className="absolute top-3 right-3" aria-label="Cerrar menú"><X aria-hidden="true" /></Button>
              </DialogPrimitive.Close>
              {children}
            </motion.div>
          </DialogPrimitive.Content>
        )}
      </AnimatePresence>
    </DialogPrimitive.Portal>
  );
}
