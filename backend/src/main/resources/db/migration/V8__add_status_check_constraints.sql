ALTER TABLE bookings
    ADD CONSTRAINT bookings_status_check
        CHECK (status IN ('awaiting_shop', 'confirmed', 'completed', 'cancelled'));

ALTER TABLE job_requests
    ADD CONSTRAINT job_requests_status_check
        CHECK (status IN ('pending', 'offers_received', 'booked', 'cancelled'));

ALTER TABLE shop_offers
    ADD CONSTRAINT shop_offers_status_check
        CHECK (status IN ('pending', 'accepted', 'rejected'));

ALTER TABLE repair_shops
    ADD CONSTRAINT repair_shops_status_check
        CHECK (status IN ('draft', 'active', 'rejected'));
