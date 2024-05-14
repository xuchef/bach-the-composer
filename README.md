# bach-the-composer

Output from running `lein run`:

```
What's up Ronny!

I had some free time this weekend so I thought I'd learn some Clojure.

I'm taking a databases class this term and in it we were recently introduced to relational algebra.
Basically, it's a formal way of representing database queries, and it forms the foundation of SQL queries.
There were some exercises in the lecture notes for us to practice constructing queries.
However, there was no way to verify if our answers were correct or not—I prefer to learn in a REPL environment :)

So, that's what this guy's for: an interactive way to view relational algebra.
I'll walk you through some of the functionality. Feel free to play around with it!

--------------------------------------------------
1. Basic Functionality
--------------------------------------------------
For this demo, we'll be working with three tables: User, Group, and Member.

User
-----------------------------
|uid|name    |age|popularity|
-----------------------------
|142|Bart    |10 |0.9       |
|857|Lisa    |8  |0.7       |
|123|Milhouse|10 |0.2       |
|456|Ralph   |8  |0.3       |
-----------------------------

Group
--------------------------
|gid|name                |
--------------------------
|abc|A Book Club         |
|gov|Student Government  |
|dps|Dead Putting Society|
--------------------------

Member
---------
|uid|gid|
---------
|857|abc|
|123|gov|
|857|gov|
|456|abc|
|456|gov|
|142|dps|
---------

As you can see, we can display them nicely with `print-table`.
--------------------------------------------------
2. Core Operators
--------------------------------------------------
Here are the core operators that form the crux of relational algebra.

Renaming: 𝜌(new-name) R
𝜌(Homies) User
-----------------------------
|uid|name    |age|popularity|
-----------------------------
|142|Bart    |10 |0.9       |
|857|Lisa    |8  |0.7       |
|123|Milhouse|10 |0.2       |
|456|Ralph   |8  |0.3       |
-----------------------------

Selection: σ(p) R
σ(popularity>0.5) User
-------------------------
|uid|name|age|popularity|
-------------------------
|142|Bart|10 |0.9       |
|857|Lisa|8  |0.7       |
-------------------------

Projection: π(L) R
π({name,age}) User
--------------
|age|name    |
--------------
|8  |Lisa    |
|10 |Milhouse|
|10 |Bart    |
|8  |Ralph   |
--------------

Cross-Product: R × S
 * Note that duplicate columns in two tables are disambiguated when taking the cross-product.
User × Member
-------------------------------------------------
|User.uid|name    |age|popularity|Member.uid|gid|
-------------------------------------------------
|456     |Ralph   |8  |0.3       |123       |gov|
|456     |Ralph   |8  |0.3       |456       |gov|
|857     |Lisa    |8  |0.7       |123       |gov|
|123     |Milhouse|10 |0.2       |857       |gov|
|456     |Ralph   |8  |0.3       |456       |abc|
|123     |Milhouse|10 |0.2       |456       |abc|
|456     |Ralph   |8  |0.3       |857       |gov|
|456     |Ralph   |8  |0.3       |142       |dps|
|857     |Lisa    |8  |0.7       |456       |abc|
|142     |Bart    |10 |0.9       |857       |gov|
|142     |Bart    |10 |0.9       |123       |gov|
|857     |Lisa    |8  |0.7       |857       |abc|
|123     |Milhouse|10 |0.2       |142       |dps|
|142     |Bart    |10 |0.9       |142       |dps|
|857     |Lisa    |8  |0.7       |857       |gov|
|857     |Lisa    |8  |0.7       |142       |dps|
|123     |Milhouse|10 |0.2       |456       |gov|
|857     |Lisa    |8  |0.7       |456       |gov|
|142     |Bart    |10 |0.9       |456       |gov|
|123     |Milhouse|10 |0.2       |123       |gov|
|456     |Ralph   |8  |0.3       |857       |abc|
|142     |Bart    |10 |0.9       |456       |abc|
|123     |Milhouse|10 |0.2       |857       |abc|
|142     |Bart    |10 |0.9       |857       |abc|
-------------------------------------------------

For the last two core operators, the table schemas must be the same, so let us introduce two new tables:

A
---------
|uid|gid|
---------
|857|abc|
|857|gov|
|456|abc|
|456|gov|
---------

B
---------
|uid|gid|
---------
|123|gov|
|456|abc|
|456|gov|
|142|dps|
---------

Union: R ∪ S
A ∪ B
---------
|uid|gid|
---------
|857|abc|
|123|gov|
|857|gov|
|456|abc|
|456|gov|
|142|dps|
---------

--------------------------------------------------
3. Derived operators
--------------------------------------------------
There's a number of useful operations that we can get by building on top of the core operators.
Intersection: R ∩ S
A ∩ B
---------
|uid|gid|
---------
|857|abc|
|857|gov|
---------

Theta-join: R ⋈(p) S
 * Equivalent to σ(p) (R × S)
User ⋈(User.uid<Member.uid) Member
-------------------------------------------------
|User.uid|name    |age|popularity|Member.uid|gid|
-------------------------------------------------
|123     |Milhouse|10 |0.2       |857       |gov|
|123     |Milhouse|10 |0.2       |456       |abc|
|456     |Ralph   |8  |0.3       |857       |gov|
|142     |Bart    |10 |0.9       |857       |gov|
|123     |Milhouse|10 |0.2       |142       |dps|
|123     |Milhouse|10 |0.2       |456       |gov|
|142     |Bart    |10 |0.9       |456       |gov|
|456     |Ralph   |8  |0.3       |857       |abc|
|142     |Bart    |10 |0.9       |456       |abc|
|123     |Milhouse|10 |0.2       |857       |abc|
|142     |Bart    |10 |0.9       |857       |abc|
-------------------------------------------------

Natural-join: R ⋈ S
 * Equivalent to R ⋈(R.i==S.i for every commmon column i) S, followed by removing the duplicate column(s)
User ⋈ Member
---------------------------------
|uid|popularity|age|name    |gid|
---------------------------------
|142|0.9       |10 |Bart    |dps|
|456|0.3       |8  |Ralph   |gov|
|456|0.3       |8  |Ralph   |abc|
|857|0.7       |8  |Lisa    |abc|
|123|0.2       |10 |Milhouse|gov|
|857|0.7       |8  |Lisa    |gov|
---------------------------------

--------------------------------------------------
4. Your turn!
--------------------------------------------------
Thanks for taking the time to go through all this :)
Feel free to play around withe the functions to create some wacky queries.
I'm writing this message 30 mins before my next databases lecture, so stay tuned for more to come!