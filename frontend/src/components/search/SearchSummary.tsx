type SearchSummaryProps = {
  filters: string[];
};

export default function SearchSummary({ filters }: SearchSummaryProps) {
  return (
    <section className="border-t border-white/40 pt-6">
      {" "}
      <h2 className="text-lg leading-7 text-white sm:text-lg font-light ">
        {" "}
        Current search:{" "}
      </h2>{" "}
      <div className="mt-6 flex flex-wrap gap-3">
        {" "}
        {filters.map((filter) => (
          <span
            key={filter}
            className="rounded-full px-4 py-2 text-sm bg-(--pills-background) shadow ring-1 ring-inset ring-white/10 hover:ring-(--accent)  hover:bg-(--accent)/40  transition-colors duration-300"
          >
            {" "}
            {filter}{" "}
          </span>
        ))}{" "}
      </div>{" "}
    </section>
  );
}
