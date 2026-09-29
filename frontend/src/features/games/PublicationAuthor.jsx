import { Link } from 'react-router-dom';
import InitialAvatar from '@/components/InitialAvatar';

const dateFormatter = new Intl.DateTimeFormat('es-AR', {
  day: '2-digit', month: 'short', year: 'numeric',
});

export default function PublicationAuthor({ handle, createdAt }) {
  return (
    <div className="flex min-w-0 flex-wrap items-center gap-x-4 gap-y-2">
      <Link to={`/users/${encodeURIComponent(handle)}`} className="flex min-h-11 min-w-0 items-center gap-3 rounded-md text-foreground hover:text-primary">
        <InitialAvatar handle={handle} />
        <span className="min-w-0 break-all text-sm font-medium">@{handle}</span>
      </Link>
      <time dateTime={createdAt} className="text-sm leading-5 text-muted-foreground">{dateFormatter.format(new Date(createdAt))}</time>
    </div>
  );
}
