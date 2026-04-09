WITH category AS (
    SELECT
        language,
        genre,
        publisher,
        COUNT(*) AS total_books
    FROM
        stl.book
    GROUP BY
        language, genre, publisher
    HAVING
        COUNT(*) > 10
),
customer_books AS (
    SELECT
        P.custid,
        B.language,
        B.genre,
        B.publisher,
        COUNT(DISTINCT P.bookid) AS bought_books
    FROM
        stl.purchase P,
        stl.book B
    WHERE
        P.bookid = B.bookid
        AND P.custid < 500
    GROUP BY
        P.custid, B.language, B.genre, B.publisher
)
SELECT
    CB.custid,
    CB.language,
    CB.genre,
    CB.publisher,
FROM
    customer_books CB,
    category CA
WHERE
    CB.language = CA.language
    AND CB.genre = CA.genre
    AND CB.publisher = CA.publisher
    AND CB.bought_books = CA.total_books
ORDER BY
    CB.custid,
    CB.language,
    CB.genre,
    CB.publisher;
