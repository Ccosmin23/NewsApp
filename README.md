# NewsApp

### Cerințe funcționale
1. Fiecare utilizator poate selecta din interfața programului său (CLI) domeniile preferate (0..3).

2. Un utilizator poate:
	a._ _ introduce o nouă știre care va fi transmisă celor interesați
	b._ _ introduce un flux mare de știri (~ 1000 mesaje)
	c.__ afișa ultimele N știri dintr-un anumit domeniu
	d.__ șterge din sistem o știre (introdusă de el)
	e. intra sau ieși oricând din sistem

3. Dacă o știre nou introdusă există deja în sistem, ea va fi ignorată.

4. O știre poate fi marcată ca fiind importantă, caz în care ea este persistentă (re-apare în
sistem la fiecare relansare).

5. O știre dacă nu este importantă se va șterge automat după o perioadă de timp prestabilită.

### Cerințe non-funcționale
1. Sistemul se va implementa în Java CLI (consolă / linie de comandă).

2. Nu se va implementa interfață grafică (GUI, web).

3. Nu se vor utiliza baze de date (MySQL, MongoDB, PostgreSQL), exceptie: Redis.

4. Se va evidenția toleranța la defectare.

5. Se va implementa un sistem de log-uri pentru a se urmări mesajele d


