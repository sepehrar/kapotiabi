import { useTranslations } from "next-intl";

export default function PopularCategories() {
  const t = useTranslations("popularCategories");
  const categories = t.raw("items") as string[];

  return (
    <section aria-labelledby="popular-categories-heading" className="bg-black">
      <div className="mx-auto w-full max-w-screen-2xl px-6 py-16 sm:px-12 lg:px-16 lg:py-24">
        <div className="mx-auto max-w-2xl text-center">
          <h2
            id="popular-categories-heading"
            className="text-2xl tracking-tight text-white sm:text-3xl text-balance font-light"
          >
            {t("title")}
          </h2>
        </div>

        <div
          className="mx-auto mt-12 
        flex flex-row flex-wrap items-center justify-center gap-6 max-w-4xl"
        >
          {categories.map((category) => (
            <a
              key={category}
              href="#"
              className="rounded-full px-4 py-2 text-sm bg-(--pills-background)  shadow ring-1 ring-inset ring-white/10 hover:ring-(--accent)  hover:bg-(--accent)/40  transition-colors duration-300"
            >
              {category}
            </a>
          ))}
        </div>

        <div className="mx-auto max-w-2xl text-center">
          <p className="mt-12 mx-auto max-w-lg text-base text-gray-300 text-balance">
            {t("description")}
          </p>
        </div>
      </div>
    </section>
  );
}
