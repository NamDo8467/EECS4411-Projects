SELECT
	C.custid,
	C.country,
	C.province,
	C.city,
	B.genre,
	SUM(P.sale) AS total_spent
FROM
	stl.customer C,
	stl.book B,
	stl.purchase P
WHERE
	C.custid = P.custid
	AND P.bookid = B.bookid
	AND C.country = 'Canada'
	AND B.genre = 'Romance'
GROUP BY
	C.custid, C.country, C.province, C.city, B.genre
ORDER BY
	total_spent DESC
LIMIT 10; 
