export default function LibrarySummary({ total, completed, favorites }) {
  return (
    <dl className="grid grid-cols-3 gap-3 border-y border-border py-6 sm:gap-8">
      {[[total, 'En biblioteca'], [completed, 'Terminados'], [favorites, 'Favoritos']].map(([value, label]) => (
        <div className="flex min-w-0 flex-col-reverse gap-2" key={label}>
          <dt className="text-sm leading-5 text-muted-foreground">{label}</dt><dd className="text-3xl leading-9 font-semibold tabular-nums text-foreground">{value}</dd>
        </div>
      ))}
    </dl>
  );
}
