import InitialAvatar from './InitialAvatar';

export default function ProfileIdentity({ handle, description }) {
  return (
    <header className="flex items-start gap-4 sm:gap-6">
      <InitialAvatar handle={handle} className="size-14 text-xl sm:size-16 sm:text-2xl" />
      <div className="grid min-w-0 gap-3">
        <h1 className="text-[28px] leading-[34px] font-semibold tracking-[-0.02em] [overflow-wrap:anywhere] md:text-4xl md:leading-[42px]">{handle}</h1>
        <p className="max-w-[640px] text-base leading-6 text-muted-foreground">{description}</p>
      </div>
    </header>
  );
}
