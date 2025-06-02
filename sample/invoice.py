import random
from datetime import datetime, timedelta

# Configuration
num_pos = 50
num_invoices = 200
start_po_date = datetime(2024, 1, 1)
end_po_date = datetime(2024, 12, 31)
start_inv_date = datetime(2024, 1, 1)
end_inv_date = datetime(2025, 5, 29)

suppliers = ["Acme Corp", "Global Industries", "Tech Solutions", "SupplyCo", "Warehouse Ltd"]

statuses = ["PENDING", "APPROVED", "RECEIVED", "CANCELLED"]

# Helper to generate random date
def random_date(start, end):
    return start + timedelta(days=random.randint(0, (end - start).days))

# Generate DDL
ddl = """
-- DDL: Create tables
CREATE TABLE purchase_orders (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    order_number     NVARCHAR(20)    NOT NULL,
    supplier         NVARCHAR(100)   NOT NULL,
    order_date       DATE            NOT NULL,
    total_amount     DECIMAL(10,2)   NOT NULL,
    status           NVARCHAR(20)    NOT NULL);

CREATE TABLE invoices (
    id                 INT IDENTITY(1,1) PRIMARY KEY,
    invoice_number     NVARCHAR(20)    NOT NULL,
    invoice_date       DATE            NOT NULL,
    amount             DECIMAL(10,2)   NOT NULL,
    purchase_order_id  INT             NULL);
"""

# Generate DML for POs
po_inserts = ["-- DML: Insert sample purchase orders"]
for i in range(1, num_pos + 1):
    order_num = f"PO{i:04d}"
    supplier = random.choice(suppliers)
    date = random_date(start_po_date, end_po_date).date()
    amount = round(random.uniform(1000, 10000), 2)
    status = random.choice(statuses)
    po_inserts.append(
        f"INSERT INTO purchase_orders (order_number, supplier, order_date, total_amount, status) "
        f"VALUES ('{order_num}', '{supplier}', '{date}', {amount}, '{status}');"
    )

# Generate DML for invoices
inv_inserts = ["\n-- DML: Insert sample invoices"]
for i in range(1, num_invoices + 1):
    inv_num = f"INV{i:05d}"
    date = random_date(start_inv_date, end_inv_date).date()
    amount = round(random.uniform(100, 10000), 2)
    # 70% of invoices link to a PO
    if random.random() < 0.7:
        po_id = random.randint(1, num_pos)
        inv_inserts.append(
            f"INSERT INTO invoices (invoice_number, invoice_date, amount, purchase_order_id) "
            f"VALUES ('{inv_num}', '{date}', {amount}, {po_id});"
        )
    else:
        inv_inserts.append(
            f"INSERT INTO invoices (invoice_number, invoice_date, amount, purchase_order_id) "
            f"VALUES ('{inv_num}', '{date}', {amount}, NULL);"
        )

# Combine and print
print(ddl)
print("\n".join(po_inserts))
print("\n".join(inv_inserts))
