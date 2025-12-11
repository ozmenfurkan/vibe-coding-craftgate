import { forwardRef, useEffect, useState } from 'react';
import Input, { InputProps } from '@/components/ui/Input';
import { formatCardNumber } from '@/lib/formatters';

interface CardInputProps extends Omit<InputProps, 'onChange'> {
  onChange: (value: string) => void;
}

/**
 * Card Input Component with automatic formatting
 * Her 4 rakamda bir boşluk ekler
 */
const CardInput = forwardRef<HTMLInputElement, CardInputProps>(
  ({ value, onChange, ...props }, ref) => {
    const [displayValue, setDisplayValue] = useState('');

    useEffect(() => {
      if (typeof value === 'string') {
        setDisplayValue(formatCardNumber(value));
      }
    }, [value]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const rawValue = e.target.value.replace(/\D/g, ''); // Sadece rakamlar
      onChange(rawValue);
    };

    return (
      <Input
        ref={ref}
        {...props}
        value={displayValue}
        onChange={handleChange}
        maxLength={23} // 19 digits + 4 spaces
        placeholder="1234 5678 9012 3456"
        inputMode="numeric"
      />
    );
  }
);

CardInput.displayName = 'CardInput';

export default CardInput;

