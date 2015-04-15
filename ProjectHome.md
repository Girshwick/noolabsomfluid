Self-Organizing Map (SOM) are usually established on fixed grids, using a 4n or 6n topology. The fixed grid prohibits a "natural" growth or differentiation of the SOM-layer. This impossibility to differentiate also renders structural learning impossible (see also: http://theputnamprogram.wordpress.com/2011/10/20/growth/).

These limitations are overcome by our SomFluid. Instead of fixed grid, we use a quasi-crystalline fluid of particles. This makes it very easy to add or to remove, to merge or to split "nodes". The grid will always take a state of minimized tensions (after shaking it a bit :)

There is yet another advantage: The fluid layer contains particles that not necessarily are identical to the nodes of the SOM, and also the relations between nodes are not bound to the hosting grid.

So far available: the fluid substrate (class "RepulsionField").
The RepulsionField class allows for a confined space or for a borderless topology (a torus), the second of which is often more suitable to run a SOM. The arrangement of the particles in such a field is almost hexagonal, dependent on parameters. Adding or removing particles immediately lead to a minimized change of the spatial arrangement, regardless the particle affected or where the change takes place.
The particles provide data structures that allow to link external catalogs of objects to them. The RepulsionField supports the usual operations necessary to run a SOM, i.e. mainly determining dynamically the neighborhood of a particular particle or location. It also offers a (growing) list of advanced features.