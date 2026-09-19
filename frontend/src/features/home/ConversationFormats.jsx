import { useId, useRef, useState } from 'react';

const formats = [
  { title: 'Dudas', heading: 'Una mano, justo donde estás', description: 'Pedí ayuda con un combate, una mecánica o una decisión del tramo que estás jugando.', guidance: 'Contá qué intentaste y elegí el checkpoint de tu consulta. Así, quienes puedan leerla sabrán hasta dónde conversar.' },
  { title: 'Reflexiones', heading: 'Lo que te dejó ese tramo', description: 'Compartí una impresión sobre lo que acabás de jugar y conversá con quienes llegaron hasta ahí.', guidance: 'Elegí el checkpoint al que se refiere tu reflexión y mantené el texto dentro de ese límite.' },
  { title: 'Teorías', heading: 'Conectá las pistas que ya encontraste', description: 'Proponé una interpretación a partir de lo que conocés de la historia hasta tu checkpoint.', guidance: 'Separá las pistas de tus suposiciones. Al responder, respetá el tramo de la publicación aunque hayas avanzado más.' },
  { title: 'Reseñas', heading: 'Tu mirada sobre el juego', description: 'Contá qué te gustó, qué no y cómo fue tu experiencia con el juego.', guidance: 'Elegí un checkpoint que cubra lo que contás. Si hablás de la historia completa, ubicá la reseña en el tramo final.' },
];

export default function ConversationFormats() {
  const id = useId();
  const [selected, setSelected] = useState(0);
  const tabs = useRef([]);

  function handleKeyDown(event, index) {
    const next = event.key === 'ArrowRight' ? (index + 1) % formats.length
      : event.key === 'ArrowLeft' ? (index + formats.length - 1) % formats.length
        : event.key === 'Home' ? 0 : event.key === 'End' ? formats.length - 1 : null;
    if (next === null) return;
    event.preventDefault();
    setSelected(next);
    tabs.current[next]?.focus();
  }

  return (
    <section className="page-container grid gap-8 py-12 md:grid-cols-[minmax(0,1fr)_minmax(0,1.4fr)] md:gap-12 md:py-16" aria-labelledby={`${id}-heading`}>
      <header className="grid content-start gap-4">
        <h2 id={`${id}-heading`} className="text-2xl leading-[30px] font-semibold">Un foro para compartir lo que jugás</h2>
        <p className="max-w-[440px] leading-6 text-muted-foreground">Una pregunta, una idea o una experiencia. Elegí cómo empezar la conversación; cada publicación tiene su checkpoint.</p>
      </header>
      <div className="min-w-0">
        <div role="tablist" aria-label="Formatos de conversación" className="flex overflow-x-auto border-b border-border">
          {formats.map((format, index) => (
            <button key={format.title} ref={(element) => { tabs.current[index] = element; }} type="button" role="tab"
              id={`${id}-tab-${index}`} aria-controls={`${id}-panel-${index}`} aria-selected={selected === index}
              tabIndex={selected === index ? 0 : -1} onClick={() => setSelected(index)} onKeyDown={(event) => handleKeyDown(event, index)}
              className="min-h-11 shrink-0 border-b-2 border-transparent px-2 py-3 text-sm font-medium text-muted-foreground transition-colors duration-150 hover:text-foreground focus-visible:outline-offset-[-3px] aria-selected:border-primary aria-selected:text-foreground sm:px-4">
              {format.title}
            </button>
          ))}
        </div>
        {formats.map((format, index) => (
          <div key={format.title} role="tabpanel" id={`${id}-panel-${index}`} aria-labelledby={`${id}-tab-${index}`} hidden={selected !== index} tabIndex={0} className="pt-6 focus-visible:outline-2 focus-visible:outline-ring focus-visible:outline-offset-4">
            <h3 className="text-xl leading-7 font-semibold">{format.heading}</h3>
            <p className="mt-3 leading-6">{format.description}</p>
            <p className="mt-6 border-l-2 border-primary pl-4 text-sm leading-6 text-muted-foreground">{format.guidance}</p>
          </div>
        ))}
      </div>
    </section>
  );
}
