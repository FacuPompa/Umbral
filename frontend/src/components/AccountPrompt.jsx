import * as Dialog from '@radix-ui/react-dialog';
import { Link } from 'react-router-dom';
import { ModalFrame } from './ui/modal-frame';
import { Button } from './ui/button';

export default function AccountPrompt({ action, onClose, from, fallbackFocusRef }) {
  return (
    <Dialog.Root open={Boolean(action)} onOpenChange={(open) => { if (!open) onClose(); }}>
      <ModalFrame primitive={Dialog} open={Boolean(action)} fallbackFocusRef={fallbackFocusRef}>
        <Dialog.Title className="text-2xl leading-[30px] font-semibold">Creá una cuenta para {action}</Dialog.Title>
        <Dialog.Description className="text-base leading-6 text-muted-foreground">Con una cuenta podés guardar tu punto del juego y Umbral muestra solo las conversaciones que ya son seguras para vos.</Dialog.Description>
        <div className="mt-1 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <Dialog.Close asChild><Button variant="outline" type="button">Seguir mirando</Button></Dialog.Close>
          <Button asChild><Link state={{ from }} to="/register">Crear cuenta</Link></Button>
        </div>
      </ModalFrame>
    </Dialog.Root>
  );
}
