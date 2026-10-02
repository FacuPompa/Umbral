import InitialAvatar from './InitialAvatar';

export default function ProfileIdentity({ handle, description, children }) {
  return (
    <header className="grid grid-cols-[auto_minmax(0,1fr)] items-start gap-x-4 gap-y-6 sm:gap-x-6">
      <InitialAvatar handle={handle} className="size-16 text-2xl sm:size-20 sm:text-3xl" />
      <div className="grid min-w-0 gap-3 self-center">
        <h1 className="text-[28px] leading-[34px] font-semibold tracking-[-0.02em] [overflow-wrap:anywhere] md:text-4xl md:leading-[42px]">{handle}</h1>
      </div>
      <p className="col-span-2 max-w-[640px] text-base leading-6 text-muted-foreground sm:col-start-2 sm:col-span-1">{description}</p>
      {children && <div className="col-span-2 flex flex-wrap gap-3 sm:col-start-2 sm:col-span-1">{children}</div>}
    </header>
  );
}
