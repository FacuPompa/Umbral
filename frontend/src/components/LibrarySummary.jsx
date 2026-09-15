export default function LibrarySummary({ total, completed, favorites }) {
  return (
    <dl className="flex flex-wrap gap-x-8 gap-y-4 text-sm leading-5">
      {[[total, 'En biblioteca'], [completed, 'Terminados'], [favorites, 'Favoritos']].map(([value, label]) => (
        <div className="flex items-baseline gap-2" key={label}>
          <dt className="text-muted-foreground">{label}</dt><dd className="font-semibold text-foreground">{value}</dd>
        </div>
      ))}
    </dl>
  );
}
