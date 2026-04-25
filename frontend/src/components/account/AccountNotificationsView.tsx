"use client";

import { useState } from "react";
import Link from "next/link";
import { useTranslations } from "next-intl";

function AccentCheckbox({
  checked,
  onChange,
}: {
  checked: boolean;
  onChange: (value: boolean) => void;
}) {
  return (
    <div className="flex h-7 shrink-0 items-center">
      <div className="group grid size-6 grid-cols-1">
        <input
          type="checkbox"
          checked={checked}
          onChange={(event) => onChange(event.target.checked)}
          className="col-start-1 row-start-1 size-6 appearance-none rounded-md border border-white/15 bg-white/5 checked:border-(--accent) checked:bg-(--accent) hover:border-white/30 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-(--accent) forced-colors:appearance-auto"
        />
        <svg
          fill="none"
          viewBox="0 0 14 14"
          className="pointer-events-none col-start-1 row-start-1 size-4 self-center justify-self-center stroke-(--accent-foreground)"
        >
          <path
            d="M3 8L6 11L11 3.5"
            strokeWidth={2.5}
            strokeLinecap="round"
            strokeLinejoin="round"
            className={checked ? "opacity-100" : "opacity-0"}
          />
        </svg>
      </div>
    </div>
  );
}

const statusStyles: Record<Status, string> = {
  new: "bg-white/10 text-white/40",
  accepted: "bg-(--accent)/20 text-(--accent)",
  booked: "bg-green-500/20 text-green-400",
};

const requests = [
  {
    id: 1,
    title: "Oil change in Tallinn",
    subtitle: "BMW 320d 2018 · OEM-equivalent · 10 km",
    sentAt: "Today, 14:32",
    status: "booked" as Status,
    replies: [
      {
        id: 1,
        shop: "AutoExpert Tallinn",
        avatar: "AE",
        time: "14:45",
        price: "€95",
        message:
          "We can take your car tomorrow at 10:00 or 14:00. OEM-equivalent parts, work takes about 1.5 hours.",
        booked: true,
      },
      {
        id: 2,
        shop: "Nord Service",
        avatar: "NS",
        time: "15:10",
        price: "From €80",
        message:
          "Available this week. We use budget parts but quality is guaranteed.",
        booked: false,
      },
    ],
  },
  {
    id: 2,
    title: "Tire service in Tartu",
    subtitle: "Audi A4 2019 · Budget · 15 km",
    sentAt: "Yesterday, 10:15",
    status: "accepted" as Status,
    replies: [
      {
        id: 3,
        shop: "FixCar Center",
        avatar: "FC",
        time: "11:02",
        price: "€110",
        message:
          "We can do it Friday or Saturday morning. Please confirm which time suits you.",
        booked: false,
      },
    ],
  },
  {
    id: 3,
    title: "A/C service in Pärnu",
    subtitle: "Volkswagen Passat 2017 · OEM · 20 km",
    sentAt: "3 days ago",
    status: "new" as Status,
    replies: [],
  },
];

export default function AccountNotificationsView() {
  const t = useTranslations("accountNotifications");
  const [openId, setOpenId] = useState<number | null>(1);

  return (
    <div className="mx-auto w-full max-w-6xl">
      <div className="pt-4">
        <h1 className="text-2xl font-light tracking-tight text-white text-balance sm:text-3xl">
          {t("title")}
        </h1>
        <p className="mt-6 max-w-xl text-lg leading-8 text-white/50 text-balance">
          {t("description")}
        </p>
      </div>

      <div className="mt-12 space-y-4">
        {requests.map((req) => {
          const isOpen = openId === req.id;
          const hasBooked = req.replies.some((r) => r.booked);

          return (
            <section
              key={req.id}
              className="rounded-2xl border border-white/10 bg-(--pills-background) overflow-hidden"
            >
              {/* Header */}
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 p-6">
                <button
                  type="button"
                  onClick={() => setOpenId(isOpen ? null : req.id)}
                  className="flex items-start gap-4 text-left flex-1 min-w-0"
                >
                  <svg
                    className={`mt-1 shrink-0 size-4 text-white/30 transition-transform duration-200 ${isOpen ? "rotate-180" : ""}`}
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    strokeWidth={1.5}
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="m19.5 8.25-7.5 7.5-7.5-7.5"
                    />
                  </svg>
                  <div>
                    <h2 className="text-xl font-semibold text-white">
                      {req.title}
                    </h2>
                    <p className="mt-1 text-base text-white/50">
                      {req.subtitle}
                    </p>
                    <p className="mt-1 text-sm text-white/30">{req.sentAt}</p>
                  </div>
                </button>

                <div className="flex flex-row sm:flex-col items-center sm:items-end gap-3 shrink-0">
                  <span
                    className={`rounded-full px-3 py-1 text-xs font-medium ${statusStyles[req.status]}`}
                  >
                    {t(`statuses.${req.status}`)}
                  </span>
                  {req.replies.length > 0 && (
                    <span className="text-sm text-white/30">
                      {req.replies.length}{" "}
                      {t(
                        req.replies.length === 1
                          ? "replies.one"
                          : "replies.other",
                      )}
                    </span>
                  )}
                  {req.status === "accepted" && (
                    <button
                      type="button"
                      onClick={() => setOpenId(req.id)}
                      className="inline-flex items-center justify-center rounded-full bg-(--accent) px-5 py-2 text-sm font-semibold text-(--accent-foreground) ring-1 ring-inset ring-(--accent) transition hover:bg-(--accent-hover)"
                    >
                      {t("actions.bookTime")}
                    </button>
                  )}
                  {req.status === "booked" && (
                    <span className="text-sm text-green-400 font-medium">
                      ✓ {t("actions.appointmentConfirmed")}
                    </span>
                  )}
                </div>
              </div>

              {/* Replies */}
              {isOpen && (
                <div className="border-t border-white/10 p-6 space-y-3">
                  {req.replies.length === 0 ? (
                    <p className="text-base text-white/30 text-center py-6">
                      {t("replies.empty")}
                    </p>
                  ) : (
                    req.replies.map((reply) => (
                      <div
                        key={reply.id}
                        className={`flex gap-4 items-start rounded-2xl p-4 transition-all ${
                          reply.booked
                            ? "ring-1 ring-inset ring-green-500/30 bg-green-500/5"
                            : "ring-1 ring-inset ring-white/5 bg-white/2 opacity-50"
                        }`}
                      >
                        {/* Avatar */}
                        <div
                          className={`shrink-0 size-10 rounded-full flex items-center justify-center text-xs font-bold border ${
                            reply.booked
                              ? "bg-green-500/10 border-green-500/20 text-green-400"
                              : "bg-gray-800 border-white/10 text-white/30"
                          }`}
                        >
                          {reply.avatar}
                        </div>

                        {/* Left: name + message | Right: price + buttons */}
                        <div className="flex-1 flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4 min-w-0">
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2 mb-2">
                              <span
                                className={`text-sm font-semibold ${reply.booked ? "text-white" : "text-white/50"}`}
                              >
                                {reply.shop}
                              </span>
                              <span className="text-xs text-white/30">
                                {reply.time}
                              </span>
                              {reply.booked && (
                                <span className="rounded-full bg-green-500/20 px-2.5 py-0.5 text-xs font-medium text-green-400">
                                  {t("statuses.booked")}
                                </span>
                              )}
                            </div>
                            <p
                              className={`text-sm leading-relaxed ${reply.booked ? "text-white/70" : "text-white/40"}`}
                            >
                              {reply.message}
                            </p>
                          </div>

                          {/* Price + actions */}
                          <div className="flex flex-row sm:flex-col items-center sm:items-end gap-2 shrink-0">
                            <p
                              className={`text-base font-semibold ${reply.booked ? "text-green-400" : "text-(--accent)"}`}
                            >
                              {reply.price}
                            </p>

                            {!reply.booked && (
                              <>
                                {hasBooked ? (
                                  <button
                                    type="button"
                                    disabled
                                    className="inline-flex items-center justify-center rounded-full px-5 py-2 text-sm font-semibold text-white/20 ring-1 ring-inset ring-white/10 cursor-not-allowed"
                                  >
                                    {t("actions.bookTime")}
                                  </button>
                                ) : (
                                  <Link
                                    href={`/booking/${req.id}`}
                                    className="inline-flex items-center justify-center rounded-full bg-(--accent) px-5 py-2 text-sm font-semibold text-(--accent-foreground) ring-1 ring-inset ring-(--accent) transition hover:bg-(--accent-hover)"
                                  >
                                    {t("actions.bookTime")}
                                  </Link>
                                )}
                                <button
                                  type="button"
                                  disabled={hasBooked}
                                  className="shrink-0 rounded-full bg-(--pills-background) px-5 py-2 text-sm text-white/60 shadow ring-1 ring-inset ring-white/10 transition-colors duration-300 hover:bg-(--accent)/40 hover:ring-(--accent) hover:text-white disabled:opacity-30 disabled:cursor-not-allowed"
                                >
                                  {t("actions.decline")}
                                </button>
                              </>
                            )}
                          </div>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              )}
            </section>
          );
        })}
      </div>
    </div>
  );
}
