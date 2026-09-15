import { useId } from 'react';
import { Search } from 'lucide-react';
import { Field, Input } from './ui/field';
import { Button } from './ui/button';
import LoadingIndicator from './LoadingIndicator';

export default function SearchForm({ query, onQueryChange, onSubmit, loading, label, placeholder, hint, maxLength = 80 }) {
  const id = useId();
  return (
    <form onSubmit={onSubmit} aria-busy={loading}>
      <Field id={id} label={label} hint={hint}>
        <div className="grid gap-3 sm:grid-cols-[minmax(0,1fr)_auto]">
          <Input id={id} aria-describedby={hint ? `${id}-hint` : undefined} autoComplete="off" maxLength={maxLength} minLength={2}
            required value={query} placeholder={placeholder} onChange={(event) => onQueryChange(event.target.value)} />
          <Button type="submit" disabled={loading || query.trim().length < 2}>
            {loading ? <LoadingIndicator label="Buscando" showLabel /> : <><Search aria-hidden="true" />Buscar</>}
          </Button>
        </div>
      </Field>
    </form>
  );
}
