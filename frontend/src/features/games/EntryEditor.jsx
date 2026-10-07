import { useEffect, useRef, useState } from 'react';
import { Pencil } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/field';
import LoadingIndicator from '@/components/LoadingIndicator';
import StatusMessage from '@/components/StatusMessage';
import { updateJournalEntry } from './gameApi';
import { entryTypeLabels } from './journalEntryTypes';

export default function EntryEditor({ entry, onSaved }) {
  const [editing, setEditing] = useState(false);
  const [type, setType] = useState(entry.type);
  const [content, setContent] = useState(entry.content);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const editButton = useRef(null);
  const contentInput = useRef(null);
  const mounted = useRef(false);
  const inFlight = useRef(false);

  useEffect(() => {
    mounted.current = true;
    return () => { mounted.current = false; };
  }, []);

  useEffect(() => {
    if (editing) contentInput.current?.focus();
  }, [editing]);

  function startEditing() {
    setType(entry.type);
    setContent(entry.content);
    setError(null);
    setEditing(true);
  }

  function cancelEditing() {
    if (inFlight.current) return;
    setEditing(false);
    requestAnimationFrame(() => editButton.current?.focus());
  }

  async function save(event) {
    event.preventDefault();
    if (inFlight.current || !content.trim()) return;
    inFlight.current = true;
    setSaving(true);
    setError(null);
    try {
      await updateJournalEntry(entry.id, type, content);
      if (mounted.current) onSaved();
    } catch (requestError) {
      if (mounted.current) setError(requestError.message);
    } finally {
      inFlight.current = false;
      if (mounted.current) setSaving(false);
    }
  }

  return (
    <div className="grid gap-4">
      {!editing && <Button ref={editButton} variant="ghost" className="w-fit px-0" type="button" onClick={startEditing}>
        <Pencil aria-hidden="true" />Editar
      </Button>}
      {editing && (
        <form id={`edit-entry-${entry.id}`} className="grid min-w-0 gap-4 border-t border-border pt-4" onSubmit={save} aria-busy={saving} aria-label="Editar publicación">
          <fieldset disabled={saving} className="grid min-w-0 gap-4">
            <legend className="sr-only">Editar publicación</legend>
            <label className="grid gap-2 text-sm font-medium" htmlFor={`edit-type-${entry.id}`}>
              Tipo de publicación
              <select id={`edit-type-${entry.id}`} className="min-h-11 min-w-0 rounded-md border border-input bg-popover px-3 py-2 text-base text-foreground focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
                value={type} onChange={(event) => setType(event.target.value)} required>
                {Object.entries(entryTypeLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </select>
            </label>
            <label className="grid gap-2 text-sm font-medium" htmlFor={`edit-content-${entry.id}`}>
              Texto de la publicación
              <Textarea ref={contentInput} id={`edit-content-${entry.id}`} value={content} maxLength={5000} required
                aria-describedby={`edit-hint-${entry.id}`} onChange={(event) => setContent(event.target.value)} />
            </label>
            <p id={`edit-hint-${entry.id}`} className="text-sm leading-5 text-muted-foreground">El checkpoint no cambia. No agregues información posterior a este tramo.</p>
            <p className="text-right text-sm tabular-nums text-muted-foreground">{content.length.toLocaleString('es-AR')} / 5.000 caracteres</p>
          </fieldset>
          {error && <StatusMessage kind="error">{error}</StatusMessage>}
          <div className="flex flex-wrap gap-3">
            <Button type="submit" disabled={saving || !content.trim() || (type === entry.type && content === entry.content)}>
              {saving ? <LoadingIndicator label="Guardando cambios" showLabel /> : 'Guardar cambios'}
            </Button>
            <Button variant="outline" type="button" disabled={saving} onClick={cancelEditing}>Cancelar</Button>
          </div>
        </form>
      )}
    </div>
  );
}
