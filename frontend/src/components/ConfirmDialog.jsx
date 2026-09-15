import * as AlertDialog from '@radix-ui/react-alert-dialog';
import { ModalFrame } from './ui/modal-frame';
import { Button } from './ui/button';
import LoadingIndicator from './LoadingIndicator';
import StatusMessage from './StatusMessage';

export default function ConfirmDialog({ open, onOpenChange, title, description, confirmLabel, pendingLabel, busy = false, error, onConfirm, destructive = false, fallbackFocusRef }) {
  return (
    <AlertDialog.Root open={open} onOpenChange={(value) => { if (!busy) onOpenChange(value); }}>
      <ModalFrame primitive={AlertDialog} open={open} busy={busy} fallbackFocusRef={fallbackFocusRef}>
        <AlertDialog.Title className="text-2xl leading-[30px] font-semibold">{title}</AlertDialog.Title>
        <AlertDialog.Description className="text-base leading-6 text-muted-foreground">{description}</AlertDialog.Description>
        {error && <StatusMessage kind="error">{error}</StatusMessage>}
        <div className="mt-1 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <AlertDialog.Cancel asChild><Button type="button" variant="outline" disabled={busy}>Cancelar</Button></AlertDialog.Cancel>
          <AlertDialog.Action asChild>
            <Button type="button" variant={destructive ? 'destructive' : 'primary'} className={destructive ? 'border border-destructive' : ''}
              disabled={busy} onClick={(event) => { event.preventDefault(); if (!busy) onConfirm(); }}>
              {busy ? <LoadingIndicator label={pendingLabel} showLabel /> : confirmLabel}
            </Button>
          </AlertDialog.Action>
        </div>
      </ModalFrame>
    </AlertDialog.Root>
  );
}
