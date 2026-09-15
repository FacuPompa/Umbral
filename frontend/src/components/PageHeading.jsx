export default function PageHeading({ title, description, children }) {
  return (
    <header className="flex flex-wrap items-end justify-between gap-5">
      <div className="grid min-w-0 gap-3">
        <h1 className="break-words text-[28px] leading-[34px] font-semibold tracking-[-0.02em] md:text-4xl md:leading-[42px]">{title}</h1>
        {description && <p className="max-w-[660px] text-base leading-6 text-muted-foreground">{description}</p>}
      </div>
      {children}
    </header>
  );
}
