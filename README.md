# Project Instructions

## Setup

To install Java 21 using PowerShell, you can use **Chocolatey**, a package manager for Windows. Follow these steps:

1. **Open PowerShell with administrator privileges:**

    - Press **Win + S** (Windows key + S) and type **PowerShell**.
    - Right-click on **Windows PowerShell** from the search results.
    - Select **"Run as Administrator"**.

2. **Install Chocolatey (if not already installed):**

    ```powershell
    Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
    ```

3. **Install Java 21:**

   After installing Chocolatey, run the following command in PowerShell to install Java 21:

    ```powershell
    choco install openjdk --version=21
    ```

4. **Verify the installation:**

   Once the installation is complete, you can verify that Java 21 was installed correctly by running:

    ```powershell
    java -version
    ```

   If the output shows a version lower than 21, it may be necessary to add the `bin` directory of your Java installation to the system's `Path` environment variable.

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

---

## Run the Program
After building the project, you can run the program using (Hint:To open a PowerShell window in the desired directory, hold **Shift**, right-click in the folder, and select the option **"Open PowerShell window here."**):
    
   ```powershell
   java -jar snark.jar
   ```
