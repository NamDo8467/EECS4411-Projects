SELECT
    C.country,
    B.language,
    SUM(P.sale) AS total_sales
FROM
    stl.purchase P,
    stl.customer C,
    stl.book B
WHERE
    P.custid = C.custid
    AND P.bookid = B.bookid
    AND P.whenp BETWEEN '2012-01-01' AND '2012-01-10'
GROUP BY
    C.country,
    B.language
ORDER BY
    total_sales DESC;
