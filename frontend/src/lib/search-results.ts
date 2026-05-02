import type { PartsQuality, SearchFilters } from "@/lib/search-chat";

export type SearchResultSeed = {
  id: number;
  name: string;
  price: string;
  distance: string;
  rating: string;
  reviews: number;
  updatedAt: string;
  city: string;
  priceValue: number;
  distanceValue: number;
  ratingValue: number;
  serviceKeywords: string[];
  makeKeywords: string[];
  modelKeywords: string[];
  areaKeywords: string[];
  partsQualities: PartsQuality[];
};

type ServiceAlias = {
  canonical: string;
  patterns: string[];
};

const SERVICE_QUERY_ALIASES: ServiceAlias[] = [
  {
    canonical: "oil change",
    patterns: ["oil change", "oil service", "olivahetus", "oli vahetus"],
  },
  {
    canonical: "maintenance",
    patterns: ["maintenance", "service", "hooldus", "korraline hooldus"],
  },
  {
    canonical: "diagnostics",
    patterns: ["diagnostics", "diagnostic", "diagnostika"],
  },
  {
    canonical: "inspection",
    patterns: ["inspection", "kontroll", "ulevaatus"],
  },
  {
    canonical: "brake repair",
    patterns: [
      "brake repair",
      "brake service",
      "piduriremont",
      "piduri remont",
      "pidurihooldus",
      "piduri hooldus",
    ],
  },
  {
    canonical: "a/c service",
    patterns: [
      "a/c service",
      "ac service",
      "air conditioning",
      "kliimahooldus",
      "kliima hooldus",
      "kliimaseade",
      "konditsioneer",
    ],
  },
  {
    canonical: "engine service",
    patterns: [
      "engine service",
      "engine repair",
      "mootorihooldus",
      "mootori hooldus",
      "mootoriremont",
      "mootori remont",
    ],
  },
];

export const SEARCH_RESULT_CATALOG: SearchResultSeed[] = [
  {
    id: 1,
    name: "AutoExpert Tallinn",
    price: "€95",
    distance: "3.2 km",
    rating: "4.7",
    reviews: 214,
    updatedAt: "Updated 2 days ago",
    city: "Tallinn",
    priceValue: 95,
    distanceValue: 3.2,
    ratingValue: 4.7,
    serviceKeywords: ["oil change", "maintenance", "inspection", "service"],
    makeKeywords: ["bmw", "audi", "volkswagen"],
    modelKeywords: ["3 series", "f30", "f31", "320d"],
    areaKeywords: ["kristiine", "kristiine keskus", "lillekula", "mustamae"],
    partsQualities: ["budget", "oem_equivalent", "oem"],
  },
  {
    id: 2,
    name: "Nord Service",
    price: "From €80",
    distance: "5.6 km",
    rating: "4.5",
    reviews: 98,
    updatedAt: "Updated today",
    city: "Tallinn",
    priceValue: 80,
    distanceValue: 5.6,
    ratingValue: 4.5,
    serviceKeywords: ["oil change", "maintenance", "diagnostics", "service"],
    makeKeywords: ["bmw", "toyota", "volvo", "skoda"],
    modelKeywords: ["3 series", "f30", "corolla", "xc60"],
    areaKeywords: ["mustamae", "kristiine", "haabersti"],
    partsQualities: ["budget", "oem_equivalent"],
  },
  {
    id: 3,
    name: "FixCar Center",
    price: "€110",
    distance: "6.8 km",
    rating: "4.8",
    reviews: 156,
    updatedAt: "Updated 5 days ago",
    city: "Tallinn",
    priceValue: 110,
    distanceValue: 6.8,
    ratingValue: 4.8,
    serviceKeywords: ["oil change", "brake repair", "a/c service", "maintenance"],
    makeKeywords: ["bmw", "mercedes", "audi"],
    modelKeywords: ["3 series", "f31", "c-class"],
    areaKeywords: ["kesklinn", "stockmann", "lillekula"],
    partsQualities: ["oem_equivalent", "oem"],
  },
  {
    id: 4,
    name: "Kristiine Auto Hub",
    price: "€105",
    distance: "2.1 km",
    rating: "4.6",
    reviews: 121,
    updatedAt: "Updated yesterday",
    city: "Tallinn",
    priceValue: 105,
    distanceValue: 2.1,
    ratingValue: 4.6,
    serviceKeywords: ["oil change", "maintenance", "engine service"],
    makeKeywords: ["bmw", "audi", "mercedes"],
    modelKeywords: ["3 series", "f30", "a4"],
    areaKeywords: ["kristiine", "kristiine keskus", "tihase"],
    partsQualities: ["oem_equivalent", "oem"],
  },
  {
    id: 5,
    name: "Tartu Garage",
    price: "€85",
    distance: "4.4 km",
    rating: "4.4",
    reviews: 77,
    updatedAt: "Updated today",
    city: "Tartu",
    priceValue: 85,
    distanceValue: 4.4,
    ratingValue: 4.4,
    serviceKeywords: ["oil change", "maintenance", "service"],
    makeKeywords: ["bmw", "toyota", "ford"],
    modelKeywords: ["3 series", "focus"],
    areaKeywords: ["kesklinn", "raudteejaam"],
    partsQualities: ["budget", "oem_equivalent"],
  },
];

export function normalizeSearchText(value: string | null | undefined) {
  return value
    ?.normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .trim()
    .toLowerCase() ?? "";
}

export function tokenizeServiceQuery(serviceQuery: string | null) {
  const normalizedQuery = normalizeSearchText(serviceQuery);

  if (!normalizedQuery) {
    return [];
  }

  const tokens = new Set(
    normalizedQuery.split(/[\s,/.-]+/).filter(Boolean),
  );

  for (const alias of SERVICE_QUERY_ALIASES) {
    if (alias.patterns.some((pattern) => normalizedQuery.includes(pattern))) {
      for (const token of alias.canonical.split(/[\s,/.-]+/).filter(Boolean)) {
        tokens.add(token);
      }
    }
  }

  return [...tokens];
}

function countKeywordMatches(tokens: string[], keywords: string[]) {
  return tokens.reduce((count, token) => {
    const matched = keywords.some((keyword) => {
      const normalizedKeyword = normalizeSearchText(keyword);

      return (
        normalizedKeyword.includes(token) || token.includes(normalizedKeyword)
      );
    });

    return count + (matched ? 1 : 0);
  }, 0);
}

export function findMatchingSearchResultSeeds(
  filters: SearchFilters,
  locationHint: string | null,
) {
  const city = normalizeSearchText(filters.city);
  const make = normalizeSearchText(filters.vehicleMake);
  const model = normalizeSearchText(filters.vehicleModel);
  const serviceTokens = tokenizeServiceQuery(filters.serviceQuery);
  const normalizedLocationHint = normalizeSearchText(locationHint);

  return SEARCH_RESULT_CATALOG.map((result) => {
    let score = 0;

    if (city && normalizeSearchText(result.city) === city) {
      score += 5;
    }

    const serviceMatches = countKeywordMatches(
      serviceTokens,
      result.serviceKeywords,
    );
    score += serviceMatches * 2;

    if (serviceTokens.length && serviceMatches === 0) {
      score -= 4;
    }

    if (
      make &&
      result.makeKeywords.some((keyword) => normalizeSearchText(keyword) === make)
    ) {
      score += 2;
    }

    if (
      model &&
      result.modelKeywords.some((keyword) => {
        const normalizedKeyword = normalizeSearchText(keyword);

        return (
          model.includes(normalizedKeyword) || normalizedKeyword.includes(model)
        );
      })
    ) {
      score += 2;
    }

    if (
      normalizedLocationHint &&
      result.areaKeywords.some((keyword) => {
        const normalizedKeyword = normalizeSearchText(keyword);

        return (
          normalizedLocationHint.includes(normalizedKeyword) ||
          normalizedKeyword.includes(normalizedLocationHint)
        );
      })
    ) {
      score += 3;
    }

    if (
      filters.partsQuality &&
      result.partsQualities.includes(filters.partsQuality)
    ) {
      score += 1;
    }

    return { result, score };
  })
    .filter(({ score, result }) => {
      if (city && normalizeSearchText(result.city) !== city) {
        return false;
      }

      return score >= 5;
    })
    .map(({ result }) => result);
}
