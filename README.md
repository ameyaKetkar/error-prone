# Error Prone - T2R

* This is a fork of error-prone which contains the T2R project. 
* T2R is a type migration tool build on top of the Google’s error-prone project.
* This artifact consists of the open source version of the tool and a subset of the open-source projects used in the evaluation (see the Notes section at the end of this document). We have readily installed T2R on a Linux VM and included the evaluation subjects for the user’s convenience. 
* [Results](https://ameyaketkar.github.io/T2RResults.html)

## T2R Demo

### Obtaining the artifact
1. Download VirtualBox desktop client.
2. Download the [VirtualBox image](https://drive.google.com/file/d/1gMIUlj04-_4qKglVJZqbKLK-EhkYcq2c/view?usp=sharing), containing a Linux Mint OS, with an environment set up to run and evaluate T2R. Import the image into VirtualBox and run it.
3. The VM has been set up to boot automatically into the OS. In case if the user credentials are needed, use the following:
* Username: T2R
* Password: icse19

### File structure
        /home/t2r/
              error-prone/			
                    /core			
                       /com/google/errorprone/bugpatterns/T2R                             *[T2R’s source code]*
              Artifacts/ 	                                *[contains the projects on which T2R was evaluated]*
                      CoreNLP/
                      neo4j/
                      presto/
                      sonarqube/


### Running the Artifact:

There is a `t2r` folder included in each open-source project’s folder (located under the `Artifacts` folder). Inside the `t2r` folder there are three scripts: 
* `SpecializeFIWitT2R.sh`: This is the main script to run T2R on each of the projects. This includes executing the three phases of the approach and applying the generated patches on the projects to migrate the types, as discussed in the paper. The following steps are followed when executing this script:
It builds the project under analysis and collects information from each compilation unit in the form of a Type-Fact-Graph (i.e., TFG). 
It triggers T2R's analysis phase which analyses the collected graphs for safe type migrations. 
It triggers a build and generates patch files to transform the source code. 
It applies the generated patches.

* `TestSuccess.sh`: This script builds the project with the migrated source code. *If the build triggered by this script is successful, the type migration applied on the source code is type-safe*

* `visualize.sh`: It visualizes the TFGs that were used to create the changes (note: this step is optional, and is very resource intensive). The TFG*.svg files can be found under the ‘t2r’ folder of the respective project. 

Note 1: To run the scripts, one might have to grant permission to the scripts using `chmod +x`. Alternatively, the scripts can be executed by running `bash [script-name]`, or `sh [script-name]`

Note 2: Running `TestSuccess.sh` and `visualize.sh` might produce an ‘IllegalThreadException’ which can be safely ignored. 

Note 3: Please run `SpecializeFIWitT2R.sh`, then `TestSuccess.sh` and finally `visualize.sh`.


### Optional
 
* To view the changes applied by T2R, one can use the GitKraken Git Client, we have synced this git client with the repositories artifacts being evaluated. The client shows the changed files on the right side of the screen and changes in the central pane. This video highlights the basic know-how of this git client.

* To evaluate a project again, one would have to reverse all the changes applied by T2R and also delete all the files produced by T2R in the `t2r` folder. This can easily be performed by discarding all changes using GitKraken git client. 

* In the [paper](https://ameyaketkar.github.io/T2R_ICSE2019.pdf), the evaluation of the tool for assessing its correctness on migrating types was done on specializing Java 8 functional interfaces. We provide the necessary [mappings](https://github.com/ameyaKetkar/error-prone/blob/master/core/src/main/java/com/google/errorprone/bugpatterns/T2R/Analysis/Migrate.java) for such migrations (i.e., the transformation specifications for the types being migrated). To try new mappings, you can create a [Program](https://github.com/ameyaKetkar/error-prone/blob/master/core/src/main/java/com/google/errorprone/bugpatterns/T2R/Analysis/Migrate.java#L227) protocol-buffer object and add it to the mappings list (the error-prone project will have to be built again).


#### Notes

As mentioned in the paper, T2R was evaluated on 7 open-source projects, in addition to Google’s proprietary source code of 300M lines of Java code, which we cannot include in this artifact. In addition, the open-source projects used to evaluate T2R are large (over 500 KLOC) and will need an immense amount of time to be built on a VM (for example, completely building the neo4j project required 33 minutes). As a result, we have not included all the open-source evaluation subjects used in the paper in this artifact, showcasing only 4 out of the 7 projects (we chose these 4 in particular since we wanted to highlight the variety of patches that T2R’s analysis can produce). 

Note that T2R’s approach needs to build the source code twice, once for the collect phase and again for migrate phase (to confirm if the type migration worked correctly, we would need to build the project one more time). 

Further, we realized that it would require a lot of time to evaluate T2R upon the larger projects included in this artifact (i.e., neo4j, presto and sonarqube). Consequently, the included scripts execute T2R only upon particular sub-projects of these large projects where we found more interesting migration opportunities For CoreNLP, we have set up the scripts to run T2R on the entire project.

## DIY

### Steps to install T2R
 Checkout https://github.com/ameyaKetkar/error-prone.git locally. 
 Build the project quickly with this command: `mvn package -DskipTests`
 Checkout the evaluation artifacts: 
* [CoreNLP](https://github.com/ameyaKetkar/CoreNLP.git) and switch to branch - T2Revaluate
* [presto](https://github.com/ameyaKetkar/presto.git) and switch to branch T2REvaluation
* [neo4j](https://github.com/ameyaKetkar/neo4j.git) and switch to branch  T2REvaluation
* [java-design-patterns](https://github.com/ameyaKetkar/java-design-patterns.git) and switch to branch T2Reval
* [sonarqube](https://github.com/ameyaKetkar/SonarqubeICSEEvaluation.git) 
* [speedment](https://github.com/ameyaKetkar/SpeedmentICSEEvaluation.git)
* or any other project of your choice. 
To evaluate T2R on the chosen projects, one should fix the paths in the project’s `pom.xml`, so that error-prone (for which T2R is a plug-in) can be run during the project’s build lifecycle. You can refer [here](https://github.com/ameyaKetkar/neo4j/blob/e4248fa94eabcccca2bf2583749560e5bfbc450f/pom.xml#L833) for an example. Also, make sure to use the correct version of Maven and Java, depending on the project being evaluated. For more details on how to integrate a project’s build system with error-prone, refer [here](https://errorprone.info/docs/installation).

  
