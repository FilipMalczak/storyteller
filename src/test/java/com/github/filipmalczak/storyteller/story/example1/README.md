The point here is to maximize number of divisors of x, where x is given as:

    x = a^b + c^d

a, b, c and d are integers; for the sake of example lets assume that calculating x is a expensive computation, as well
as calculating the number of its divisors.

We'll keep a, b, c, d and x in files (named with these letters, so value of a will be in file "a").

We'll keep number of divisors in a Nitrite document indexed with value of x.

For the sake of showing the value in this tool, calculating x and finding the number of divisors has a delay of random 
number on ms (from 0 to 5000).

Several consecutive reruns of this test (first one starting with empty workspace) took following times:
- 4m20s
- 17s
- 20s
- 19s
- 22s

Looking into the workspace itself, I found out that there were 159 scenes 
(which checks out - 3 scenes to initialize, then 4 variables*(16-3 possibile values)*3 scenes).

Add a story task, initialization thread task, 4 choice tasks and 13 research tasks for each of them (so, 1+1+4+4*13)
and we get 217 tasks in total.

Now, lets take the highest re-execution time - 22s. Given ~220 tasks, that gives us an overhead of about 100ms per task.

> All that happened on Linux Mint 20.2, with IntelliJ running, as well as Chromium with over a dozen tabs. The machine
> had 32GB RAM and Intel i7. Not a very powerful hardware, but not a pocket calculator either. 