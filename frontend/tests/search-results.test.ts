import assert from "node:assert/strict";
import test from "node:test";

import { createEmptySearchFilters } from "../src/lib/search-chat.js";
import {
  findMatchingSearchResultSeeds,
  tokenizeServiceQuery,
} from "../src/lib/search-results.js";

test("tokenizeServiceQuery expands Estonian service aliases into canonical search terms", () => {
  const tokens = tokenizeServiceQuery("Õlivahetus");

  assert(tokens.includes("olivahetus"));
  assert(tokens.includes("oil"));
  assert(tokens.includes("change"));
});

test("Estonian oil change queries return demo results in Tallinn", () => {
  const filters = {
    ...createEmptySearchFilters(),
    serviceQuery: "Õlivahetus",
    city: "Tallinn",
    vehicleMake: "Volkswagen",
    vehicleModel: "Passat",
  };

  const results = findMatchingSearchResultSeeds(filters, null);

  assert(results.length > 0);
  assert.equal(results[0]?.city, "Tallinn");
});
