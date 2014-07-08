itemsByProductId=select * from item where product_id = ?
itemsByCustomerId=SELECT * FROM ITEM WHERE INVOICE_ID IN (SELECT INVOICE_ID FROM INVOICE WHERE CUSTOMER_ID = ?)