# Project Instructions

## Setup

To install Java 23 using PowerShell, you can use **Chocolatey**, a package manager for Windows. Follow these steps:

1. **Open PowerShell with administrator privileges:**

    - Press **Win + S** (Windows key + S) and type **PowerShell**.
    - Right-click on **Windows PowerShell** from the search results.
    - Select **"Run as Administrator"**.

2. **Install Chocolatey (if not already installed):**

    ```powershell
    Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
    ```

3. **Install Java 23:**

   After installing Chocolatey, run the following command in PowerShell to install Java 23:

    ```powershell
   choco install openjdk --version=23
    ```

4. **Verify the installation:**

   Once the installation is complete, you can verify that Java 23 was installed correctly by running:

    ```powershell
    java -version
    ```

   If the output shows a version lower than 23, it may be necessary to add the `bin` directory of your Java installation to the system's `Path` environment variable.

---

## Build the Project

To build the project, execute the following command in the project's root directory:

```powershell
mvn clean package
```

---

## Configure the Project

1. **Set up the server:**

    - Specify the server you want to play on.
    - Request a hash key from the server administrator and enter it in the `main.hash` file.

2. **Update credentials:**

    - Enter your login credentials (username and password).
    - Update the server name as needed.

3. **Database configuration:**

   The application supports two database variants:

   - PostgreSQL mode: use `-Ddatabase-name=<database_name>`
     ```powershell
     java -Ddatabase-name=my_database -jar snark.jar
     ```
     In this variant, the app connects to PostgreSQL on localhost and uses the provided database name.

   - H2 file mode: use `-Ddatabase-file=<path_to_h2_file>`
     ```powershell
     java -Ddatabase-file=./db/snark -jar snark.jar
     ```
     If the file does not exist, the application creates it automatically. If the value is empty or omitted, the default file name `snark` is created in the current working directory.

   You may use either one of these parameters depending on your setup. If neither is set, the app falls back to the local H2 file database behavior.

---

## Run the Program

After building the project, you can run the program from the project root using one of the supported variants:

### PostgreSQL

```powershell
java -Ddatabase-name=my_database -jar snark.jar
```

### H2 file

```powershell
java -Ddatabase-file=./db/snark -jar snark.jar
```

If you want the default local H2 database file, you can also run:

```powershell
java -jar snark.jar
```

Hint: To open a PowerShell window in the desired directory, hold **Shift**, right-click in the folder, and select **"Open PowerShell window here."**.
