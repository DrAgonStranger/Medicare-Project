from machine import Pin, I2C
import bluetooth
from ble_simple_peripheral import BLESimplePeripheral
import time

# Create a Bluetooth Low Energy (BLE) object
ble = bluetooth.BLE()
# Create an instance of the BLESimplePeripheral class with the BLE object
sp = BLESimplePeripheral(ble)

# Set the debounce time to 0. Used for switch debouncing
debounce_time = 0

# MPU6050 registers
MPU6050_ADDR = 0x68
PWR_MGMT_1 = 0x6B
TEMP_OUT_H = 0x41
ACCEL_XOUT_H = 0x43
GYRO_XOUT_H = 0x3B


# Function to initialize I2C communication
def init_i2c():
    i2c = I2C(1, scl=Pin(27), sda=Pin(26), freq=400000)  # Pico's I2C1
    return i2c


# Function to write a byte to a register
def write_byte(i2c, addr, reg, data):
    i2c.writeto_mem(addr, reg, bytearray([data]))


# Function to read a 16-bit signed value from a register
def read_word(i2c, addr, reg):
    data = i2c.readfrom_mem(addr, reg, 2)
    value = (data[0] << 8) | data[1]
    return value if value < 0x8000 else value - 0x10000  # Convert to signed


# Function to initialize MPU6050
def init_mpu6050(i2c):
    write_byte(i2c, MPU6050_ADDR, PWR_MGMT_1, 0)  # Wake up the MPU6050


# Function to read accelerometer values
def read_accel(i2c):
    accel_x = read_word(i2c, MPU6050_ADDR, ACCEL_XOUT_H)
    accel_y = read_word(i2c, MPU6050_ADDR, ACCEL_XOUT_H + 2)
    accel_z = read_word(i2c, MPU6050_ADDR, ACCEL_XOUT_H + 4)
    return accel_x, accel_y, accel_z


# Function to read gyroscope values
def read_gyro(i2c):
    gyro_x = read_word(i2c, MPU6050_ADDR, GYRO_XOUT_H)
    gyro_y = read_word(i2c, MPU6050_ADDR, GYRO_XOUT_H + 2)
    gyro_z = read_word(i2c, MPU6050_ADDR, GYRO_XOUT_H + 4)
    return gyro_x, gyro_y, gyro_z


# Function to read temperature
def read_temp(i2c):
    temp_raw = read_word(i2c, MPU6050_ADDR, TEMP_OUT_H)
    temp_celsius = (temp_raw / 340.0) + 36.53  # MPU6050 temperature conversion
    return temp_celsius


# Main loop
def main():
    i2c = init_i2c()
    init_mpu6050(i2c)

    counter = 0

    while True:

        accel_x, accel_y, accel_z = read_accel(i2c)
        if abs(accel_x) + abs(accel_y) + abs(accel_z) >= 20000:
            counter += 1
        gyro_x, gyro_y, gyro_z = read_gyro(i2c)
        temp_celsius = read_temp(i2c)
        print("Accelerometer X:", accel_x, " Y:", accel_y, " Z:", accel_z)
        print("Gyroscope X:", gyro_x, " Y:", gyro_y, " Z:", gyro_z)
        print("Steps", counter)

        # Check if the BLE connection is established
        if sp.is_connected():
            # Create a counter string
            cnt = counter
            # Send the counter via BLE
            sp.send(cnt)

        time.sleep(0.25)


if __name__ == "__main__":
    main()
