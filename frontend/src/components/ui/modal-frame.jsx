import { useRef } from 'react';
import { AnimatePresence, m, useReducedMotion } from 'motion/react';

// Dialog y AlertDialog comparten geometría y transición, no su semántica.
export function ModalFrame({ primitive: Primitive, open, busy, children, fallbackFocusRef }) {
  const previousFocus = useRef(null);
  const reduced = useReducedMotion();
  const transition = { duration: reduced ? 0 : 0.14 };
  function restoreFocus(event) {
    event.preventDefault();
    const opener = previousFocus.current;
    const target = opener?.isConnected && !opener.disabled ? opener : fallbackFocusRef?.current;
    target?.focus({ preventScroll: true });
  }
  return (
    <Primitive.Portal forceMount>
      <AnimatePresence>
        {open && <Primitive.Overlay key="overlay" forceMount asChild>
          <m.div className="fixed inset-0 z-50 bg-black/40" initial={{ opacity: reduced ? 1 : 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} transition={transition} />
        </Primitive.Overlay>}
        {open && <Primitive.Content key="content" forceMount asChild
          onOpenAutoFocus={() => { previousFocus.current = document.activeElement; }}
          onCloseAutoFocus={restoreFocus} onEscapeKeyDown={(event) => { if (busy) event.preventDefault(); }}>
          <m.div className="fixed top-1/2 left-1/2 z-50 grid max-h-[calc(100dvh-32px)] w-[calc(100%-32px)] max-w-[520px] -translate-x-1/2 -translate-y-1/2 gap-5 overflow-y-auto rounded-lg border border-border bg-popover p-6 text-popover-foreground shadow-overlay sm:p-8"
            initial={{ opacity: reduced ? 1 : 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} transition={transition}>
            {children}
          </m.div>
        </Primitive.Content>}
      </AnimatePresence>
    </Primitive.Portal>
  );
}
