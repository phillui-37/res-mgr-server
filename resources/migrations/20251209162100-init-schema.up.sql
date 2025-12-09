-- normal table
CREATE TABLE IF NOT EXISTS location(
    id BLOB NOT NULL, -- uuidv7
    name VARCHAR(200) NOT NULL,
    path TEXT NOT NULL,
    client_type VARCHAR(200) NOT NULL,
    PRIMARY KEY (id, name, client_type)
);
--;;
CREATE TABLE IF NOT EXISTS tag(
    id BLOB NOT NULL, --uuidv7
    name VARCHAR(200) NOT NULL,
    PRIMARY KEY (id, name)
);
--;;
CREATE TABLE IF NOT EXISTS category(
    id BLOB NOT NULL, -- uuidv7
    name VARCHAR(200) NOT NULL,
    PRIMARY KEY (id, name)
);
--;;
CREATE TABLE IF NOT EXISTS item(
    id BLOB NOT NULL, -- uuidv7
    category_id BLOB NOT NULL,
    is_safe INTEGER NOT NULL, -- bool
    -- prop will not use json, use separated tables combination instead
    FOREIGN KEY (category_id) REFERENCES category(id),
    PRIMARY KEY (id)
);
--;;
CREATE TABLE IF NOT EXISTS prop(
    id BLOB NOT NULL, -- uuidv7
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL, -- type hint for parsing
    PRIMARY KEY (id)
);
--;;
-- M-M table
CREATE TABLE IF NOT EXISTS item_tag_map(
    item_id BLOB NOT NULL,
    tag_id BLOB NOT NULL,
    FOREIGN KEY (item_id) REFERENCES item(id),
    FOREIGN KEY (tag_id) REFERENCES tag(id),
    PRIMARY KEY (item_id, tag_id)
);
--;;
CREATE TABLE IF NOT EXISTS item_location_map(
    item_id BLOB NOT NULL,
    location_id BLOB NOT NULL,
    FOREIGN KEY (item_id) REFERENCES item(id),
    FOREIGN KEY (location_id) REFERENCES location(id)
    PRIMARY KEY (item_id, location_id)
);
--;;
CREATE TABLE IF NOT EXISTS category_prop_map(
    category_id BLOB NOT NULL,
    prop_id BLOB NOT NULL,
    FOREIGN KEY (category_id) REFERENCES category(id),
    FOREIGN KEY (prop_id) REFERENCES prop(id),
    PRIMARY KEY (category_id, prop_id)
);
--;;
CREATE TABLE IF NOT EXISTS item_prop_value(
    item_id BLOB NOT NULL,
    prop_id BLOB NOT NULL,
    value TEXT NOT NULL, -- need client parse this manually
    FOREIGN KEY (item_id) REFERENCES item(id),
    FOREIGN KEY (prop_id) REFERENCES prop(id),
    PRIMARY KEY (item_id, prop_id)
);