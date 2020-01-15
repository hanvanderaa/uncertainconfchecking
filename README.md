# uncertainconfchecking

This repository accompanies the manuscript "Partial Order Resolution of Event Logs for Process Conformance Checking", by Han van der Aa, Henrik Leopold, and Matthias Weidlich, currently under submission at Decision Support Systems (DSS).

### Prerequisites

Java JRE 1.8 is required to be able to run the implementation.

The provided source code is set up as a Maven Project, which means that Maven is required to be installed. Detailed instructions on how to install maven are available at: https://maven.apache.org/install.html

### Installing

All required dependencies are provided as Maven dependencies in pom.xml.

### Data collections used in evaluation

The evaluation experiments presented in the manuscript are conducted using two data collections, one consisting of real-life event logs and one consisting of generated process models.

## Real-world collection:
As discussed in Section 6.1 of the manuscript, we used the following three real-world event logs, which can be downloaded through the provided DOIs:
- Van Dongen, B.F. (Boudewijn), Bpi challenge 2012 (2012). doi:10.4121/UUID:3926DB30-F712-4394-AEBC-75976070E91F.
- B. Van Dongen, BPI Challenge 2014 (2014). doi:10.4121/uuid:c3e5d162-0cfd-4bb0-bd82-af5268819c35.
- M. M. De Leoni, F. F. Mannhardt, Road traffic fine management process (2015). doi:10.4121/UUID:
270FD440- 1057- 4FB9- 89A9- B699B47990F5.

## Synthetic collection:
As discussed in Section 6.1 of the manuscript, we generated 500 synthetic process models and accompanying using the following work:
- T. Jouck, B. Depaire, Generating artificial data for empirical analysis of control-flow discovery algorithms: A process tree
and log generator, BISE (2018) 1–18.
All these process models are available in the folder uncertainconfchecking/CoarseGrainedChecking/input/generated/
