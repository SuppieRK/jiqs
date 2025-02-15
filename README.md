# JIQS

> The work on this software project is in no way associated with my employer nor with the role I'm having at my
> employer.
>
> I maintain this project alone and as much or as little as my **spare time** permits using my **personal** equipment.

JIQS (pronounced as "jicks," rhyming with "kicks") repository offers a template for the alternative highly customizable
Java microservice stack with focus on simplicity, modularity and control over dependencies where each piece can be
determined individually and excluded / replaced.

It opens a path for [platform engineers](https://platformengineering.org/blog/what-is-platform-engineering) to craft a
bigger abstraction, tailored for specific company business needs to reduce friction during development.

Please, review each module `README.md` files for more specific information, as well as comments in the codebase.

## What contributes to the simplicity?

- Focusing on Java language, less focus on the annotations / frameworks to stay true
  to [YAGNI](https://en.wikipedia.org/wiki/You_aren%27t_gonna_need_it) principle.
- Focusing on pure SQL, no
  ORM [for](https://martinfowler.com/bliki/OrmHate.html) [several](https://medium.com/building-the-system/dont-be-a-sucker-and-stop-using-orms-190add65add4) [reasons](https://en.wikipedia.org/wiki/Object%E2%80%93relational_impedance_mismatch).

## What are the disadvantages?

- No "plug-n-play".
- Requires more experience to make right decisions.
- No textbook definition on how to do things:
    - Potentially a breeding ground for a poorly documented internal framework with non-transferable knowledge.

## What are the advantages?

- Select the tech stack most suitable for your business needs, which usually will perform better than the generic stack.
- Better control over dependencies, which also improves security posture.
- Less framework-biased hiring process, leaving more room to explore problem-solving and language skills.