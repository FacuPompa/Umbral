import { useId } from 'react';
import { Search } from 'lucide-react';
import { Field } from './ui/field';
import { Button } from './ui/button';
import LoadingIndicator from './LoadingIndicator';

export default function SearchForm({ query, onQueryChange, onSubmit, loading, label, placeholder, hint, maxLength = 80 }) {
  const id = useId();
  return (
    <form onSubmit={onSubmit} aria-busy={loading}>
      <Field id={id} label={label} hint={hint}>
        <div className="flex min-w-0 items-center gap-2 rounded-md border border-input bg-popover p-1 pl-3 focus-within:border-ring">
          <Search aria-hidden="true" className="size-[18px] shrink-0 text-muted-foreground" />
          <input className="min-h-11 min-w-0 flex-1 bg-transparent px-1 py-2 text-base text-foreground" id={id} aria-describedby={hint ? `${id}-hint` : undefined} autoComplete="off" maxLength={maxLength} minLength={2}
            required value={query} placeholder={placeholder} onChange={(event) => onQueryChange(event.target.value)} />
          <Button type="submit" aria-label={loading ? 'Buscando' : 'Buscar'} disabled={loading || query.trim().length < 2}>
            {loading ? <LoadingIndicator label="Buscando" /> : 'Buscar'}
          </Button>
        </div>
      </Field>
    </form>
  );
}
